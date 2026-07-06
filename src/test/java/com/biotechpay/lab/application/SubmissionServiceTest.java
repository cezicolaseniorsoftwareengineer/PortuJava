package com.biotechpay.lab.application;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.biotechpay.lab.domain.ComparisonMode;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.ExerciseProgress;
import com.biotechpay.lab.domain.ExerciseStatus;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.domain.TestCase;
import com.biotechpay.lab.persistence.ExerciseProgressRepository;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class SubmissionServiceTest {

    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private LearningModuleRepository moduleRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private ExerciseProgressRepository progressRepository;
    @Autowired
    private UserService userService;

    private static final String CORRECT_SOURCE = """
            public class Adder {
                public int add(int a, int b) {
                    return a + b;
                }
            }
            """;

    private static final String WRONG_SOURCE = """
            public class Adder {
                public int add(int a, int b) {
                    return a - b;
                }
            }
            """;

    private static final String BROKEN_SOURCE = """
            public class Adder {
                public int add(int a, int b) {
                    return a + b
                }
            }
            """;

    private Exercise exercise;
    private String exerciseId;

    @BeforeEach
    void seedExercise() {
        LearningModule module = moduleRepository
                .save(new LearningModule("oop-test-module", "Modulo de teste", "OOP", "desc", 0));

        exercise = new Exercise(
                "adder-" + UUID.randomUUID(), module, "Somador",
                "Implemente Adder.add(a, b) retornando a soma dos dois numeros.",
                "public class Adder { public int add(int a, int b) { } }",
                "public class Adder {\n}\n", "INICIANTE", 0, 5);

        TestCase visibleCase = new TestCase(exercise, "soma de 2 e 3 deve ser 5",
                "Adder solution = new Adder();", "solution.add(2, 3)", "5", ComparisonMode.EQUALS, 0);

        TestCase hiddenCase = new TestCase(exercise, "soma de 10 e -4 deve ser 6",
                "Adder solution = new Adder();", "solution.add(10, -4)", "6", ComparisonMode.EQUALS, 1);
        hiddenCase.setVisible(false);

        exercise.getTestCases().add(visibleCase);
        exercise.getTestCases().add(hiddenCase);
        exercise = Objects.requireNonNull(exerciseRepository.save(exercise), "exerciseRepository.save returned null");
        exerciseId = Objects.requireNonNull(exercise.getExerciseId(), "exerciseId");
    }

    @Test
    void correctSolutionPassesAllTestsAndMarksExerciseSolved() {
        SubmissionService.SubmissionResult result = submissionService.submit(exerciseId, CORRECT_SOURCE);

        assertThat(result.compileSuccess()).isTrue();
        assertThat(result.allPassed()).isTrue();
        assertThat(result.passedTests()).isEqualTo(2);
        assertThat(result.exerciseSolved()).isTrue();

        ExerciseProgress progress = progressRepository
                .findByUserAndExercise(userService.getOrCreateDefaultUser(), exercise).orElseThrow();
        assertThat(progress.getStatus()).isEqualTo(ExerciseStatus.SOLVED);
        assertThat(progress.getFirstSolvedAt()).isNotNull();
    }

    @Test
    void wrongSolutionReportsPartialFailureAndHidesActualValueOnHiddenCase() {
        SubmissionService.SubmissionResult result = submissionService.submit(exerciseId, WRONG_SOURCE);

        assertThat(result.allPassed()).isFalse();
        assertThat(result.passedTests()).isZero();
        assertThat(result.exerciseSolved()).isFalse();
        assertThat(result.testResults()).hasSize(2);

        SubmissionService.TestCaseFeedback visibleFeedback = result.testResults().get(0);
        assertThat(visibleFeedback.hidden()).isFalse();
        assertThat(visibleFeedback.passed()).isFalse();
        assertThat(visibleFeedback.actualValueSummary()).isEqualTo("-1");

        SubmissionService.TestCaseFeedback hiddenFeedback = result.testResults().get(1);
        assertThat(hiddenFeedback.hidden()).isTrue();
        assertThat(hiddenFeedback.passed()).isFalse();
        assertThat(hiddenFeedback.actualValueSummary()).isNull();

        ExerciseProgress progress = progressRepository
                .findByUserAndExercise(userService.getOrCreateDefaultUser(), exercise).orElseThrow();
        assertThat(progress.getStatus()).isEqualTo(ExerciseStatus.IN_PROGRESS);
        assertThat(progress.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void compileErrorReportsErrorsWithoutRunningAnyTest() {
        SubmissionService.SubmissionResult result = submissionService.submit(exerciseId, BROKEN_SOURCE);

        assertThat(result.compileSuccess()).isFalse();
        assertThat(result.compileErrors()).isNotEmpty();
        assertThat(result.testResults()).isEmpty();
        assertThat(result.exerciseSolved()).isFalse();

        ExerciseProgress progress = progressRepository
                .findByUserAndExercise(userService.getOrCreateDefaultUser(), exercise).orElseThrow();
        assertThat(progress.getAttemptCount()).isEqualTo(1);
        assertThat(progress.getStatus()).isNotEqualTo(ExerciseStatus.SOLVED);
    }

    @Test
    void resubmittingAfterAlreadySolvedDoesNotRegressFirstSolvedAt() throws InterruptedException {
        submissionService.submit(exerciseId, CORRECT_SOURCE);
        LocalDateTime firstSolvedAt = progressRepository
                .findByUserAndExercise(userService.getOrCreateDefaultUser(), exercise)
                .orElseThrow().getFirstSolvedAt();

        Thread.sleep(10);
        SubmissionService.SubmissionResult secondResult = submissionService.submit(exerciseId, CORRECT_SOURCE);

        assertThat(secondResult.exerciseSolved()).isTrue();
        ExerciseProgress progress = progressRepository
                .findByUserAndExercise(userService.getOrCreateDefaultUser(), exercise).orElseThrow();
        assertThat(progress.getFirstSolvedAt()).isEqualTo(firstSolvedAt);
        assertThat(progress.getAttemptCount()).isEqualTo(2);
    }
}
