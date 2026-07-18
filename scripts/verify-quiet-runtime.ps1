[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$javaHome = $env:JAVA_HOME
$env:DEBUG = 'false'

if ([string]::IsNullOrWhiteSpace($javaHome)) {
    throw 'JAVA_HOME must be configured before running the quiet runtime verification.'
}

$javaExecutable = Join-Path $javaHome 'bin\java.exe'
if (-not (Test-Path -LiteralPath $javaExecutable -PathType Leaf)) {
    throw "Java executable was not found at '$javaExecutable'."
}

$jar = Get-ChildItem -LiteralPath (Join-Path $workspaceRoot 'target') -Filter '*.jar' -File |
    Where-Object { $_.Name -notlike '*.original' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $jar) {
    throw 'No packaged application JAR was found. Run the Maven verify gate first.'
}

$listener = Get-NetTCPConnection -LocalPort 62828 -State Listen -ErrorAction SilentlyContinue
if ($null -ne $listener) {
    throw "Port 62828 is already in use by process $($listener.OwningProcess)."
}

$runId = [Guid]::NewGuid().ToString('N')
$stdoutPath = Join-Path ([System.IO.Path]::GetTempPath()) "portujava-$runId.stdout.log"
$stderrPath = Join-Path ([System.IO.Path]::GetTempPath()) "portujava-$runId.stderr.log"
$process = $null
$failureMessage = $null

try {
    $arguments = @(
        '-jar',
        ('"{0}"' -f $jar.FullName),
        '--app.open-browser=false',
        '--debug=false',
        '--server.address=127.0.0.1',
        '--spring.datasource.url=jdbc:h2:mem:quiet-runtime;DB_CLOSE_DELAY=-1',
        '--spring.jpa.hibernate.ddl-auto=create-drop'
    )

    $processOptions = @{
        FilePath = $javaExecutable
        ArgumentList = $arguments
        WorkingDirectory = $workspaceRoot
        WindowStyle = 'Hidden'
        RedirectStandardOutput = $stdoutPath
        RedirectStandardError = $stderrPath
        PassThru = $true
    }
    $process = Start-Process @processOptions

    $ready = $false
    $deadline = [DateTime]::UtcNow.AddSeconds(60)

    while ([DateTime]::UtcNow -lt $deadline) {
        if ($process.HasExited) {
            break
        }

        try {
            $response = Invoke-WebRequest -Uri 'http://127.0.0.1:62828/' -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -eq 200) {
                $ready = $true
                break
            }
        }
        catch {
            Start-Sleep -Milliseconds 500
        }
    }

    if (-not $ready) {
        throw 'PortuJava did not become ready on http://127.0.0.1:62828/ within 60 seconds.'
    }
}
catch {
    $failureMessage = $_.Exception.Message
}
finally {
    if ($null -ne $process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
        $process.WaitForExit()
    }
}

$stdout = if (Test-Path -LiteralPath $stdoutPath) { Get-Content -Raw -LiteralPath $stdoutPath } else { '' }
$stderr = if (Test-Path -LiteralPath $stderrPath) { Get-Content -Raw -LiteralPath $stderrPath } else { '' }

if (-not [string]::IsNullOrWhiteSpace($stdout) -or -not [string]::IsNullOrWhiteSpace($stderr)) {
    throw "The runtime emitted unexpected terminal output. $failureMessage`nSTDOUT:`n$stdout`nSTDERR:`n$stderr"
}

if (-not [string]::IsNullOrWhiteSpace($failureMessage)) {
    throw $failureMessage
}

Remove-Item -LiteralPath $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue
