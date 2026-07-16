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
    void everyPublishedReferenceSolutionHasCanonicalWhitespaceAndReadableLineLengths() {
        List<Exercise> exercises = exerciseRepository.findAll();

        assertThat(exercises).hasSize(97);
        for (Exercise exercise : exercises) {
            String code = exercise.getReferenceSolution();
            assertThat(code)
                    .as("canonical whitespace for %s", exercise.getExerciseId())
                    .isEqualTo(ReferenceSolutionFormatter.format(code))
                    .doesNotContain("\r", "\t", "\n\n\n")
                    .doesNotMatch("(?s).*\\s+$");
            assertThat(code.lines())
                    .as("readable line length for %s", exercise.getExerciseId())
                    .allSatisfy(line -> assertThat(line.length()).isLessThanOrEqualTo(120));
        }
    }
}
