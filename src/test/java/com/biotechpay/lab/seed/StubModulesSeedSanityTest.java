package com.biotechpay.lab.seed;

import com.biotechpay.lab.application.SubmissionService;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Same seed-sanity guarantee as CoffeeMachineModuleSeederTest, applied to the stub modules (event-driven,
 * AOP-simulated, rule-engine, DSA, logic-shapes): every referenceSolution must compile and pass all of its
 * own test cases through the real grading path.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class StubModulesSeedSanityTest {

    @Autowired
    private LearningModuleRepository moduleRepository;
    @Autowired
    private SubmissionService submissionService;

    @ParameterizedTest
    @ValueSource(strings = {"event-driven-robot", "aop-decorator-simulation", "rule-engine-daily-routine", "dsa-foundations", "logic-shapes"})
    void everyReferenceSolutionPassesAllOfItsOwnTestCases(String moduleCode) {
        LearningModule module = moduleRepository.findByModuleCode(moduleCode).orElseThrow();
        assertThat(module.getExercises()).as("module %s must have seeded exercises", moduleCode).isNotEmpty();

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

    @Test
    void allStubModulesAreSeeded() {
        assertThat(moduleRepository.findAllByOrderBySortOrderAsc())
                .extracting("moduleCode")
                .contains("oop-coffee-machine", "event-driven-robot", "aop-decorator-simulation",
                        "rule-engine-daily-routine", "dsa-foundations", "logic-shapes");
    }
}
