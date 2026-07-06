package com.biotechpay.lab.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JavaCodeCompilerTest {

    private final JavaCodeCompiler compiler = new JavaCodeCompiler();

    @Test
    void compilesAndRunsValidSource() {
        String source = """
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("ola mundo");
                    }
                }
                """;

        JavaCodeCompiler.CompileAndRunResult result = compiler.compileAndRun(source, "Hello");

        assertThat(result.success()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.output()).isEqualTo("ola mundo");
        assertThat(result.timedOut()).isFalse();
        assertThat(result.outputTruncated()).isFalse();
    }

    @Test
    void reportsSyntaxErrorsWithoutRunning() {
        String source = """
                public class Broken {
                    public static void main(String[] args) {
                        System.out.println("missing semicolon")
                    }
                }
                """;

        JavaCodeCompiler.CompileAndRunResult result = compiler.compileAndRun(source, "Broken");

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    @Timeout(20)
    void killsAndReportsInfiniteLoop() {
        String source = """
                public class Loopy {
                    public static void main(String[] args) {
                        while (true) { }
                    }
                }
                """;

        JavaCodeCompiler.CompileAndRunResult result = compiler.compileAndRun(source, "Loopy");

        assertThat(result.success()).isFalse();
        assertThat(result.timedOut()).isTrue();
    }

    @Test
    @Timeout(20)
    void capsRunawayOutputInsteadOfHangingUntilTimeout() {
        String source = """
                public class Flooder {
                    public static void main(String[] args) {
                        while (true) {
                            System.out.println("x".repeat(1000));
                        }
                    }
                }
                """;

        long start = System.nanoTime();
        JavaCodeCompiler.CompileAndRunResult result = compiler.compileAndRun(source, "Flooder");
        long elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000L;

        assertThat(result.outputTruncated()).isTrue();
        // The output cap must kill the process well before the run timeout (8s) would.
        assertThat(elapsedSeconds).isLessThan(8);
    }

    @Test
    void cleansUpTempDirectoryOnSuccess() throws IOException {
        long before = countCompileTempDirs();

        String source = """
                public class CleanupCheck {
                    public static void main(String[] args) {
                        System.out.println("done");
                    }
                }
                """;
        compiler.compileAndRun(source, "CleanupCheck");

        assertThat(countCompileTempDirs()).isEqualTo(before);
    }

    @Test
    @Timeout(20)
    void cleansUpTempDirectoryOnTimeout() throws IOException {
        long before = countCompileTempDirs();

        String source = """
                public class CleanupOnTimeout {
                    public static void main(String[] args) {
                        while (true) { }
                    }
                }
                """;
        compiler.compileAndRun(source, "CleanupOnTimeout");

        assertThat(countCompileTempDirs()).isEqualTo(before);
    }

    @Test
    void compilesAndRunsMultiClassHarness() {
        String studentSource = """
                public class Solution {
                    public int square(int n) {
                        return n * n;
                    }
                }
                """;
        String harnessSource = """
                public class Harness {
                    public static void main(String[] args) {
                        Solution s = new Solution();
                        int actual = s.square(4);
                        if (actual == 16) {
                            System.out.println("RESULT#0#PASS");
                        } else {
                            System.out.println("RESULT#0#FAIL#" + actual);
                        }
                    }
                }
                """;

        JavaCodeCompiler.CompileAndRunResult result =
                compiler.compileAndRunHarness(studentSource, harnessSource, "Solution", "Harness");

        assertThat(result.success()).isTrue();
        assertThat(result.output()).isEqualTo("RESULT#0#PASS");
    }

    @Test
    void compileOnlySucceedsWithoutRunningAndReportsNoOutput() {
        String source = """
                public class NoMain {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;

        JavaCodeCompiler.CompileAndRunResult result = compiler.compileOnly(source, "NoMain");

        assertThat(result.success()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.output()).isEmpty();
    }

    @Test
    void compileOnlyStillReportsSyntaxErrors() {
        String source = """
                public class BrokenNoMain {
                    public int add(int a, int b) {
                        return a + b
                    }
                }
                """;

        JavaCodeCompiler.CompileAndRunResult result = compiler.compileOnly(source, "BrokenNoMain");

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    private long countCompileTempDirs() throws IOException {
        Path tempRoot = Path.of(System.getProperty("java.io.tmpdir"));
        try (Stream<Path> children = Files.list(tempRoot)) {
            return children
                    .filter(p -> p.getFileName().toString().startsWith("java_compile_"))
                    .filter(p -> new File(p.toString()).isDirectory())
                    .count();
        }
    }
}
