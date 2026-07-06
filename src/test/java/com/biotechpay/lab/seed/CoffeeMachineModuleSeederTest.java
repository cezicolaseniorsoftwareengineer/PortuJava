package com.biotechpay.lab.seed;

import com.biotechpay.lab.application.SubmissionService;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression guard for seed content: every seeded Exercise's referenceSolution must actually compile
 * and pass 100% of its own TestCases through the real SubmissionService/JavaCodeCompiler path. Without
 * this, a typo in a TestCase expression (wrong expected literal, bad invocationExpression) would only
 * surface the first time a real student hit that exercise.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class CoffeeMachineModuleSeederTest {

    @Autowired
    private LearningModuleRepository moduleRepository;
    @Autowired
    private SubmissionService submissionService;

    @Test
    void everyReferenceSolutionPassesAllOfItsOwnTestCases() {
        LearningModule module = moduleRepository.findByModuleCode("oop-coffee-machine").orElseThrow();
        assertThat(module.getExercises()).isNotEmpty();

        for (Exercise exercise : module.getExercises()) {
            assertThat(exercise.getReferenceSolution())
                    .as("exercise %s must have a referenceSolution to be seed-sanity-checked", exercise.getExerciseId())
                    .isNotNull();

            SubmissionService.SubmissionResult result =
                    submissionService.submit(exercise.getExerciseId(), exercise.getReferenceSolution());

            assertThat(result.compileSuccess())
                    .as("referenceSolution for %s must compile: %s", exercise.getExerciseId(), result.compileErrors())
                    .isTrue();
            assertThat(result.allPassed())
                    .as("referenceSolution for %s must pass all its own test cases: %s", exercise.getExerciseId(), result.testResults())
                    .isTrue();
        }
    }
}
