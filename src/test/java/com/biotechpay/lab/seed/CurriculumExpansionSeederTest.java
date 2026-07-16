package com.biotechpay.lab.seed;

import com.biotechpay.lab.application.SubmissionService;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class CurriculumExpansionSeederTest {

    @Autowired
    private LearningModuleRepository moduleRepository;

    @Autowired
    private SubmissionService submissionService;

    @Test
    void distinguishedMethodHasSixExecutableExercises() {
        LearningModule module = moduleRepository.findByModuleCode("distinguished-engineering-method").orElseThrow();

        assertThat(module.getExercises()).hasSize(6);
        assertEveryReferenceSolutionPasses(module);
    }

    @Test
    void interviewTrackHasExactlyThirtyChallengesInFourLevels() {
        LearningModule module = moduleRepository.findByModuleCode("technical-interview-preparation").orElseThrow();

        assertThat(module.getExercises()).hasSize(30);
        Map<String, Long> counts = module.getExercises().stream()
                .collect(Collectors.groupingBy(Exercise::getDifficulty, Collectors.counting()));
        assertThat(counts).containsExactlyInAnyOrderEntriesOf(Map.of(
                "BÁSICO", 8L,
                "MÉDIO", 8L,
                "INTERMEDIÁRIO", 8L,
                "AVANÇADO", 6L));
        assertThat(module.getExercises()).allSatisfy(exercise ->
                assertThat(exercise.getStatementMarkdown()).contains("Desafio original de entrevista"));
        assertEveryReferenceSolutionPasses(module);
    }

    private void assertEveryReferenceSolutionPasses(LearningModule module) {
        for (Exercise exercise : module.getExercises()) {
            SubmissionService.SubmissionResult result =
                    submissionService.submit(exercise.getExerciseId(), exercise.getReferenceSolution());
            assertThat(result.compileSuccess())
                    .as("reference solution for %s must compile: %s", exercise.getExerciseId(), result.compileErrors())
                    .isTrue();
            assertThat(result.allPassed())
                    .as("reference solution for %s must pass: %s", exercise.getExerciseId(), result.testResults())
                    .isTrue();
        }
    }
}
