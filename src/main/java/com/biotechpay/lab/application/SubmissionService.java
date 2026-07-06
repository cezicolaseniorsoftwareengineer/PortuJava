package com.biotechpay.lab.application;

import com.biotechpay.lab.domain.*;
import com.biotechpay.lab.persistence.ExerciseProgressRepository;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orchestrates exercise grading: compile the student's code, generate and run a test harness against
 * it, persist the outcome, and advance the student's progress.
 */
@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private static final Pattern PUBLIC_TYPE_NAME = Pattern.compile(
            "public\\s+(?:final\\s+|abstract\\s+|sealed\\s+|non-sealed\\s+)*(?:class|interface|enum|record)\\s+(\\w+)");
    private static final Pattern RESULT_LINE = Pattern.compile("RESULT#(\\d+)#(PASS|FAIL|EXCEPTION)(?:#(.*))?");
    private static final Pattern MAIN_METHOD = Pattern.compile(
            "(?:public\\s+static|static\\s+public)\\s+void\\s+main\\s*\\(\\s*String\\s*(?:\\[\\s*\\]|\\.\\.\\.)\\s*\\w*\\s*\\)");
    private static final String NO_MAIN_METHOD_MESSAGE =
            "Compilado com sucesso. Esta classe nao tem um metodo main - ela foi escrita para ser " +
                    "corrigida, nao executada diretamente. Clique em \"Enviar\" para rodar os testes deste desafio.";

    private final ExerciseRepository exerciseRepository;
    private final SubmissionRepository submissionRepository;
    private final ExerciseProgressRepository progressRepository;
    private final UserService userService;
    private final JavaCodeCompiler javaCodeCompiler;
    private final TestHarnessGenerator harnessGenerator;

    public SubmissionService(ExerciseRepository exerciseRepository,
                              SubmissionRepository submissionRepository,
                              ExerciseProgressRepository progressRepository,
                              UserService userService,
                              JavaCodeCompiler javaCodeCompiler,
                              TestHarnessGenerator harnessGenerator) {
        this.exerciseRepository = exerciseRepository;
        this.submissionRepository = submissionRepository;
        this.progressRepository = progressRepository;
        this.userService = userService;
        this.javaCodeCompiler = javaCodeCompiler;
        this.harnessGenerator = harnessGenerator;
    }

    @Transactional
    public SubmissionResult submit(String exerciseId, String submittedCode) {
        String requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId");
        String requiredSubmittedCode = Objects.requireNonNull(submittedCode, "submittedCode");
        Exercise exercise = exerciseRepository.findByExerciseId(requiredExerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercicio nao encontrado: " + requiredExerciseId));
        User user = userService.getOrCreateDefaultUser();

        String studentClassName = extractPublicTypeName(requiredSubmittedCode);
        if (studentClassName == null) {
            return persistAndBuildResult(user, exercise, requiredSubmittedCode,
                    new Grading(false, List.of("Nao foi possivel encontrar uma classe, interface, enum ou record publico no seu codigo."),
                            false, false, List.of()));
        }

        List<TestCase> testCases = exercise.getTestCases();
        String harnessClassName = "Harness_" + UUID.randomUUID().toString().replace("-", "");
        String harnessSource = harnessGenerator.generate(harnessClassName, testCases);

        JavaCodeCompiler.CompileAndRunResult compileAndRun =
                javaCodeCompiler.compileAndRunHarness(requiredSubmittedCode, harnessSource, studentClassName, harnessClassName);

        Grading grading = grade(exercise, testCases, studentClassName, harnessClassName, compileAndRun);
        return persistAndBuildResult(user, exercise, requiredSubmittedCode, grading);
    }

    private Grading grade(Exercise exercise, List<TestCase> testCases, String studentClassName,
                           String harnessClassName, JavaCodeCompiler.CompileAndRunResult compileAndRun) {

        if (!compileAndRun.errors().isEmpty()) {
            // javac echoes back the absolute temp-file path it was invoked with, e.g.
            // "C:\...\java_compile_xxx\Solution.java:3: error: ...", so file attribution must search
            // for the file name within the line rather than assume the line starts with it.
            String harnessFileMarker = harnessClassName + ".java:";
            boolean harnessAuthoringBug = compileAndRun.errors().stream().anyMatch(line -> line.contains(harnessFileMarker));
            if (harnessAuthoringBug) {
                log.error("Harness compile failure for exercise {} (authoring bug in a TestCase expression): {}",
                        exercise.getExerciseId(), compileAndRun.errors());
                return new Grading(false, List.of("Erro interno de correcao. Tente novamente ou contate o suporte."),
                        false, false, List.of());
            }

            List<String> studentErrors = stripAbsolutePaths(compileAndRun.errors(), studentClassName);
            return new Grading(false, studentErrors, false, false, List.of());
        }

        if (compileAndRun.timedOut()) {
            return new Grading(true, List.of(), true, false,
                    List.of());
        }

        Map<Integer, String[]> resultsByIndex = parseResultLines(compileAndRun.output());
        List<TestCaseFeedback> feedback = new ArrayList<>();
        int passedCount = 0;
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String[] parsed = resultsByIndex.get(i);
            boolean passed = parsed != null && "PASS".equals(parsed[0]);
            if (passed) {
                passedCount++;
            }
            boolean hidden = !testCase.isVisible();
            String actualValueSummary = null;
            String exceptionSummary = null;
            if (!hidden && !passed && parsed != null) {
                String detail = parsed.length > 1 ? parsed[1] : null;
                if ("FAIL".equals(parsed[0])) {
                    actualValueSummary = detail;
                } else if ("EXCEPTION".equals(parsed[0])) {
                    exceptionSummary = detail;
                }
            }
            feedback.add(new TestCaseFeedback(testCase.getDescription(), passed, hidden, actualValueSummary, exceptionSummary));
        }

        boolean allPassed = !testCases.isEmpty() && passedCount == testCases.size();
        return new Grading(true, List.of(), false, allPassed, feedback);
    }

    private Map<Integer, String[]> parseResultLines(String output) {
        Map<Integer, String[]> byIndex = new LinkedHashMap<>();
        if (output == null) {
            return byIndex;
        }
        for (String line : output.split("\n")) {
            Matcher matcher = RESULT_LINE.matcher(line.trim());
            if (matcher.matches()) {
                int index = Integer.parseInt(matcher.group(1));
                String status = matcher.group(2);
                String detail = matcher.group(3);
                byIndex.put(index, detail == null ? new String[]{status} : new String[]{status, detail});
            }
        }
        return byIndex;
    }

    private SubmissionResult persistAndBuildResult(User user, Exercise exercise, String submittedCode, Grading grading) {
        Submission submission = new Submission(user, exercise, submittedCode);
        submission.setCompileSuccess(grading.compileSuccess());
        submission.setCompileErrors(grading.compileErrors().isEmpty() ? null : String.join("\n", grading.compileErrors()));
        submission.setExecutionTimedOut(grading.timedOut());
        submission.setTotalTests(grading.testResults().size());
        submission.setPassedTests((int) grading.testResults().stream()
                .filter(testCaseFeedback -> Objects.requireNonNull(testCaseFeedback, "testCaseFeedback").passed())
                .count());
        submission.setAllPassed(grading.allPassed());
        List<TestCase> orderedTestCases = exercise.getTestCases();
        for (int i = 0; i < grading.testResults().size(); i++) {
            TestCaseFeedback tc = grading.testResults().get(i);
            Long testCaseId = i < orderedTestCases.size() ? orderedTestCases.get(i).getId() : null;
            submission.getResults().add(new TestCaseResult(testCaseId, tc.description(), tc.passed(), tc.hidden(),
                    tc.actualValueSummary(), tc.exceptionSummary()));
        }
        submissionRepository.save(submission);

        ExerciseProgress progress = progressRepository.findByUserAndExercise(user, exercise)
                .orElseGet(() -> new ExerciseProgress(user, exercise));
        progress.setAttemptCount(progress.getAttemptCount() + 1);
        progress.setLastAttemptAt(LocalDateTime.now());
        progress.setLastSubmittedCode(submittedCode);
        progress.setTotalTests(grading.testResults().size());
        progress.setBestPassedTests(Math.max(progress.getBestPassedTests(), submission.getPassedTests()));
        if (grading.allPassed()) {
            if (progress.getStatus() != ExerciseStatus.SOLVED) {
                progress.setStatus(ExerciseStatus.SOLVED);
                progress.setFirstSolvedAt(LocalDateTime.now());
            }
        } else if (progress.getStatus() != ExerciseStatus.SOLVED) {
            progress.setStatus(ExerciseStatus.IN_PROGRESS);
        }
        progressRepository.save(progress);

        if (user.getStreakDays() <= 0) {
            user.setStreakDays(1);
        }
        user.setLastPracticeDate(LocalDateTime.now());

        return new SubmissionResult(
                grading.compileSuccess(),
                grading.compileErrors(),
                grading.timedOut(),
                grading.testResults().size(),
                submission.getPassedTests(),
                grading.allPassed(),
                grading.testResults(),
                progress.getStatus() == ExerciseStatus.SOLVED
        );
    }

    /**
     * Runs the student's current editor code as-is, with no grading - the IDE's "Executar" action,
     * distinct from "Enviar" (submit). Useful for a quick sanity check before submitting for real.
     *
     * <p>Every exercise class (including a revealed referenceSolution, copy-pasted verbatim) is
     * written to be graded through a harness, not run directly - it has no {@code main} method. Java-
     * executing it anyway would surface a raw JVM "main method not found" error while still reporting
     * {@code success: true} (compilation genuinely succeeded), which reads as a broken product to a
     * student who just pasted a "complete" solution. Detect that case up front and compile-only.
     */
    public JavaCodeCompiler.CompileAndRunResult scratchRun(String code) {
        String className = extractPublicTypeName(code);
        if (className == null) {
            return new JavaCodeCompiler.CompileAndRunResult(false,
                    List.of("Nao foi possivel encontrar uma classe, interface, enum ou record publico no seu codigo."),
                    List.of(), "", false, false);
        }

        if (!MAIN_METHOD.matcher(code).find()) {
            JavaCodeCompiler.CompileAndRunResult compiled = javaCodeCompiler.compileOnly(code, className);
            if (!compiled.errors().isEmpty()) {
                return new JavaCodeCompiler.CompileAndRunResult(false,
                        stripAbsolutePaths(compiled.errors(), className), compiled.warnings(), "", false, false);
            }
            return new JavaCodeCompiler.CompileAndRunResult(true, List.of(), compiled.warnings(),
                    NO_MAIN_METHOD_MESSAGE, false, false);
        }

        JavaCodeCompiler.CompileAndRunResult result = javaCodeCompiler.compileAndRun(code, className);
        if (result.errors().isEmpty()) {
            return result;
        }
        return new JavaCodeCompiler.CompileAndRunResult(
                result.success(),
                stripAbsolutePaths(result.errors(), className),
                result.warnings(),
                result.output(),
                result.timedOut(),
                result.outputTruncated());
    }

    /**
     * javac echoes back the absolute temp-file path it was invoked with, e.g.
     * "C:\...\java_compile_xxx\Solution.java:3: error: ...". Strips everything before the file name
     * so a student never sees the server's local temp directory layout.
     */
    private static List<String> stripAbsolutePaths(List<String> lines, String className) {
        String fileMarker = className + ".java:";
        return lines.stream()
                .map(line -> line.contains(fileMarker) ? line.substring(line.indexOf(fileMarker)) : line)
                .toList();
    }

    public List<SubmissionHistoryEntry> getHistory(String exerciseId) {
        Exercise exercise = exerciseRepository.findByExerciseId(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercicio nao encontrado: " + exerciseId));
        User user = userService.getOrCreateDefaultUser();
        return submissionRepository.findByUserAndExerciseOrderBySubmittedAtDesc(user, exercise).stream()
                .map(submission -> new SubmissionHistoryEntry(
                        submission.getSubmittedAt(),
                        submission.isCompileSuccess(),
                        submission.isAllPassed(),
                        submission.getPassedTests(),
                        submission.getTotalTests()))
                .toList();
    }

    private static String extractPublicTypeName(String code) {
        if (code == null) {
            return null;
        }
        Matcher matcher = PUBLIC_TYPE_NAME.matcher(code);
        return matcher.find() ? matcher.group(1) : null;
    }

    private record Grading(boolean compileSuccess, List<String> compileErrors, boolean timedOut,
                            boolean allPassed, List<TestCaseFeedback> testResults) {}

    public record TestCaseFeedback(
            String description,
            boolean passed,
            boolean hidden,
            String actualValueSummary,
            String exceptionSummary
    ) {}

    public record SubmissionResult(
            boolean compileSuccess,
            List<String> compileErrors,
            boolean executionTimedOut,
            int totalTests,
            int passedTests,
            boolean allPassed,
            List<TestCaseFeedback> testResults,
            boolean exerciseSolved
    ) {}

    public record SubmissionHistoryEntry(
            LocalDateTime submittedAt,
            boolean compileSuccess,
            boolean allPassed,
            int passedTests,
            int totalTests
    ) {}
}
