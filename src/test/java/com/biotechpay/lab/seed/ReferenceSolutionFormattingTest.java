package com.biotechpay.lab.seed;

import com.biotechpay.lab.application.ReferenceSolutionFormatter;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.persistence.ExerciseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReferenceSolutionFormattingTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void everyPublishedJavaSurfaceHasCanonicalSyntaxAwareFormatting() {
        List<Exercise> exercises = exerciseRepository.findAll();

        assertThat(exercises).hasSize(97);
        for (Exercise exercise : exercises) {
            assertCanonicalSource(exercise, "code contract", exercise.getCodeContract());
            assertCanonicalSource(exercise, "starter code", exercise.getStarterCode());
            assertCanonicalSource(exercise, "reference solution", exercise.getReferenceSolution());
            exercise.getSolutionAnnotations().forEach(annotation ->
                    assertThat(annotation.getCodeExcerpt())
                            .as("canonical annotation excerpt for %s", exercise.getExerciseId())
                            .isEqualTo(ReferenceSolutionFormatter.formatExcerpt(
                                    exercise.getReferenceSolution(), annotation.getCodeExcerpt()))
                            .doesNotContain("\r", "\t")
                            .doesNotMatch("(?s).*\\s+$"));
        }
    }

    private static void assertCanonicalSource(Exercise exercise, String surface, String code) {
        assertThat(code)
                .as("canonical %s for %s", surface, exercise.getExerciseId())
                .isEqualTo(ReferenceSolutionFormatter.format(code))
                .doesNotContain("\r", "\t", "\n\n\n")
                .doesNotMatch("(?s).*\\s+$");
        assertThat(code.lines())
                .as("readable %s line length for %s", surface, exercise.getExerciseId())
                .allSatisfy(line -> assertThat(line.length()).isLessThanOrEqualTo(120));
    }
}
