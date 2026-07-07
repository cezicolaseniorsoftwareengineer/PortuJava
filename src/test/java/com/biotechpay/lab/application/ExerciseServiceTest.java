package com.biotechpay.lab.application;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ExerciseServiceTest {

    @Autowired
    private ExerciseService exerciseService;
    @Autowired
    private LearningModuleRepository moduleRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;

    private static final String SOLUTION_CODE = """
            public class Adder {
                public int add(int a, int b) {
                    return a + b;
                }
            }
            """;

    private Exercise exercise;

    @BeforeEach
    void seedExercise() {
        LearningModule module = moduleRepository
                .save(new LearningModule("solution-test-module", "Modulo de teste", "OOP", "desc", 0));

        exercise = new Exercise(
                "solution-" + UUID.randomUUID(), module, "Somador",
                "Implemente Adder.add(a, b).",
                "public class Adder { public int add(int a, int b) { } }",
                "public class Adder {\n}\n", "INICIANTE", 0, 5);
        exercise.setReferenceSolution(SOLUTION_CODE);
        exercise.getHints().add("Some os dois parametros.");
        exercise.getHints().add("Use o operador +.");
        exercise.getSolutionAnnotations().add(new com.biotechpay.lab.domain.SolutionAnnotation(
                "return a + b;", "A soma e o operador + entre os dois parametros recebidos."));
        exercise = exerciseRepository.save(exercise);
    }

    @Test
    void revealReturnsReferenceSolutionAndWalkthroughSteps() {
        ExerciseService.SolutionView solution = exerciseService.getSolution(exercise.getExerciseId());

        assertThat(solution.solutionCode()).isEqualTo(SOLUTION_CODE);
        assertThat(solution.steps()).containsExactly("Some os dois parametros.", "Use o operador +.");
        assertThat(solution.annotations()).hasSize(1);
        assertThat(solution.annotations().get(0).codeExcerpt()).isEqualTo("return a + b;");
        assertThat(solution.annotations().get(0).explanation())
                .isEqualTo("A soma e o operador + entre os dois parametros recebidos.");
    }

    @Test
    void revealFailsForUnknownExercise() {
        assertThatThrownBy(() -> exerciseService.getSolution("does-not-exist"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void revealFailsWhenExerciseHasNoReferenceSolution() {
        exercise.setReferenceSolution(null);
        exerciseRepository.save(exercise);

        assertThatThrownBy(() -> exerciseService.getSolution(exercise.getExerciseId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void detailPayloadStillOmitsTheReferenceSolution() {
        ExerciseService.ExerciseDetail detail = exerciseService.getExerciseDetail(exercise.getExerciseId());

        assertThat(detail.editorCode()).doesNotContain("return a + b;");
        assertThat(detail.statementMarkdown()).doesNotContain("return a + b;");
    }
}
