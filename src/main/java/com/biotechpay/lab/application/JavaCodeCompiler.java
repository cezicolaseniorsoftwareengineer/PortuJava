package com.biotechpay.lab.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Compiles and runs student-submitted Java source via {@code javac}/{@code java} in an isolated temp
 * directory.
 *
 * <p>This executes untrusted code as the core mechanism of every exercise submission. It is a local,
 * single-user development tool — the student is also the machine owner — so full sandboxing
 * (SecurityManager, containers, cgroups) is deliberately out of scope; SecurityManager is itself
 * deprecated for removal in modern JDKs. The mandatory minimum bar enforced here is: a process
 * timeout, a captured-output size cap (a fast infinite-print loop can flood output long before a
 * timeout would fire), and guaranteed temp-directory cleanup on every path, including timeouts. If
 * this application is ever bound beyond {@code localhost}, this trust boundary must be revisited.
 */
@Service
public class JavaCodeCompiler {

    private static final Logger log = LoggerFactory.getLogger(JavaCodeCompiler.class);

    private static final long COMPILE_TIMEOUT_SECONDS = 15;
    private static final long RUN_TIMEOUT_SECONDS = 8;
    private static final int MAX_OUTPUT_CHARS = 64 * 1024;

    private static final Pattern JAVAC_ERROR_LINE = Pattern.compile(".*:\\d+:\\s+(error|erro):.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVAC_WARNING_LINE = Pattern.compile(".*:\\d+:\\s+(warning|aviso):.*", Pattern.CASE_INSENSITIVE);

    public JavaCodeCompiler() {
        log.warn("JavaCodeCompiler executes student-submitted code via javac/java with no OS-level " +
                "sandboxing beyond a timeout and an output cap. Safe only while this app is bound to localhost.");
    }

    public CompileAndRunResult compileAndRun(String sourceCode, String className) {
        return compileFiles(Map.of(className, sourceCode), className, true);
    }

    /**
     * Compiles without executing - for source with no runnable entry point (see
     * SubmissionService#scratchRun: an exercise class is meant to be graded via a harness, never
     * {@code java}-executed directly, so running it would surface a raw "main method not found" JVM
     * error dressed up as a successful run).
     */
    public CompileAndRunResult compileOnly(String sourceCode, String className) {
        return compileFiles(Map.of(className, sourceCode), className, false);
    }

    /**
     * Compiles the student's class together with a generated harness class in the same temp
     * directory, then runs the harness (which drives the student's code and prints machine-readable
     * {@code RESULT#} markers). Used by exercise grading; {@link #compileAndRun} remains for the
     * ungraded "scratch run" IDE action.
     */
    public CompileAndRunResult compileAndRunHarness(String studentSource, String harnessSource,
                                                      String studentClassName, String harnessClassName) {
        Map<String, String> sources = new LinkedHashMap<>();
        sources.put(studentClassName, studentSource);
        sources.put(harnessClassName, harnessSource);
        return compileFiles(sources, harnessClassName, true);
    }

    private CompileAndRunResult compileFiles(Map<String, String> sourcesByClassName, String mainClassName,
                                              boolean runAfterCompile) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String output = "";
        boolean compileSuccess = false;
        boolean timedOut = false;
        boolean outputTruncated = false;
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("java_compile_");
            List<File> sourceFiles = new ArrayList<>();
            for (Map.Entry<String, String> entry : sourcesByClassName.entrySet()) {
                File sourceFile = new File(tempDir.toFile(), entry.getKey() + ".java");
                Files.write(sourceFile.toPath(), entry.getValue().getBytes(StandardCharsets.UTF_8));
                sourceFiles.add(sourceFile);
            }

            CompileOutcome compileOutcome = compileWithJavac(sourceFiles, tempDir.toFile(), errors, warnings);
            compileSuccess = compileOutcome.success();
            timedOut = compileOutcome.timedOut();

            if (runAfterCompile && compileSuccess && !timedOut) {
                ProcessOutcome runOutcome = executeCompiledClass(tempDir.toFile(), mainClassName);
                output = runOutcome.output();
                timedOut = runOutcome.timedOut();
                outputTruncated = runOutcome.truncated();
            }

        } catch (IOException e) {
            errors.add("Erro de I/O: " + e.getMessage());
        } catch (Exception e) {
            errors.add("Erro ao compilar: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                deleteTempDirectory(tempDir);
            }
        }

        boolean success = compileSuccess && errors.isEmpty() && !timedOut;
        return new CompileAndRunResult(success, errors, warnings, output, timedOut, outputTruncated);
    }

    private CompileOutcome compileWithJavac(List<File> sourceFiles, File outputDir, List<String> errors, List<String> warnings) {
        List<String> command = new ArrayList<>(List.of("javac", "-d", outputDir.getAbsolutePath(), "-encoding", "UTF-8"));
        for (File sourceFile : sourceFiles) {
            command.add(sourceFile.getAbsolutePath());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            ProcessOutcome outcome = runBounded(process, COMPILE_TIMEOUT_SECONDS);
            if (outcome.timedOut()) {
                errors.add("Tempo limite de compilacao excedido (" + COMPILE_TIMEOUT_SECONDS + "s).");
                return new CompileOutcome(false, true);
            }

            String compilerOutput = outcome.output().trim();
            for (String line : compilerOutput.split("\n")) {
                if (JAVAC_ERROR_LINE.matcher(line).matches()) {
                    errors.add(line);
                } else if (JAVAC_WARNING_LINE.matcher(line).matches()) {
                    warnings.add(line);
                }
            }

            boolean success = outcome.exitCode() == 0;
            if (!success && errors.isEmpty()) {
                errors.add(compilerOutput.isBlank() ? "Compilacao falhou sem diagnostico do javac." : compilerOutput);
            }

            return new CompileOutcome(success, false);

        } catch (Exception e) {
            errors.add("Falha ao compilar: " + e.getMessage());
            return new CompileOutcome(false, false);
        }
    }

    private ProcessOutcome executeCompiledClass(File classDir, String className) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", classDir.getAbsolutePath(), className);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return runBounded(process, RUN_TIMEOUT_SECONDS);

        } catch (Exception e) {
            return new ProcessOutcome("Erro ao executar: " + e.getMessage(), false, false, -1);
        }
    }

    /**
     * Runs a process to completion under a hard timeout, draining stdout on a background thread so a
     * silently-hanging process (no output, e.g. {@code while(true){}}) does not block this thread's
     * timed wait, and so a print-flooding process is capped and killed before the timeout would
     * otherwise fire.
     */
    private ProcessOutcome runBounded(Process process, long timeoutSeconds) throws InterruptedException {
        StringBuilder output = new StringBuilder();
        Object lock = new Object();
        boolean[] truncated = {false};

        Thread readerThread = new Thread(() -> {
            try (InputStreamReader reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
                char[] buffer = new char[4096];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    synchronized (lock) {
                        if (output.length() >= MAX_OUTPUT_CHARS) {
                            truncated[0] = true;
                            process.destroyForcibly();
                            break;
                        }
                        output.append(buffer, 0, read);
                    }
                }
            } catch (IOException ignored) {
                // Stream closed because the process was destroyed (timeout or truncation) - expected.
            }
        }, "java-code-compiler-output-reader");
        readerThread.setDaemon(true);
        readerThread.start();

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        boolean timedOut = !finished;
        if (timedOut) {
            process.destroyForcibly();
            // destroyForcibly() is asynchronous on Windows: block until the OS actually confirms the
            // process is gone, otherwise the caller's temp-dir cleanup can race a still-held file lock.
            process.waitFor(5, TimeUnit.SECONDS);
        }
        readerThread.join(2000);

        String finalOutput;
        boolean wasTruncated;
        synchronized (lock) {
            finalOutput = output.toString();
            wasTruncated = truncated[0];
        }
        if (wasTruncated && process.isAlive()) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
        }
        int exitCode = process.isAlive() ? -1 : process.exitValue();
        return new ProcessOutcome(finalOutput.trim(), timedOut, wasTruncated, exitCode);
    }

    private void deleteTempDirectory(Path tempDir) {
        for (int attempt = 0; attempt < 3; attempt++) {
            FileSystemUtils.deleteRecursively(tempDir.toFile());
            if (!Files.exists(tempDir)) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("Could not delete Java compiler temp directory: {}", tempDir);
    }

    private record CompileOutcome(boolean success, boolean timedOut) {}

    private record ProcessOutcome(String output, boolean timedOut, boolean truncated, int exitCode) {}

    public record CompileAndRunResult(
            boolean success,
            List<String> errors,
            List<String> warnings,
            String output,
            boolean timedOut,
            boolean outputTruncated
    ) {}
}
