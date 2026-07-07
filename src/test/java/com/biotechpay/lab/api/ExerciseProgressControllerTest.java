package com.biotechpay.lab.api;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.biotechpay.lab.application.UserService;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.ExerciseProgress;
import com.biotechpay.lab.domain.ExerciseStatus;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.domain.Submission;
import com.biotechpay.lab.domain.User;
import com.biotechpay.lab.persistence.ExerciseProgressRepository;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.persistence.SubmissionRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ExerciseProgressControllerTest {

    @Autowired
    private ExerciseProgressController controller;
    @Autowired
    private LearningModuleRepository moduleRepository;
    @Autowired
    private ExerciseRepository exerciseRepository;
    @Autowired
    private ExerciseProgressRepository progressRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private UserService userService;

    private Exercise exercise;
    private User user;

    @BeforeEach
    void seedSolvedExercise() {
        LearningModule module = moduleRepository
                .save(new LearningModule("reset-test-module", "Módulo de teste", "OOP", "desc", 0));
        exercise = exerciseRepository.save(new Exercise(
                "reset-" + UUID.randomUUID(), module, "Exercício de teste",
                "Descrição do exercício.",
                "public class Adder { public int add(int a, int b) { } }",
                "public class Adder {\n}\n", "INICIANTE", 0, 5));

        user = userService.getOrCreateDefaultUser();

        ExerciseProgress progress = new ExerciseProgress(user, exercise);
        progress.setStatus(ExerciseStatus.SOLVED);
        progress.setBestPassedTests(3);
        progress.setTotalTests(3);
        progress.setAttemptCount(2);
        progress.setLastSubmittedCode("public class Adder { public int add(int a, int b) { return a + b; } }");
        progressRepository.save(progress);

        submissionRepository.save(new Submission(user, exercise,
                "public class Adder { public int add(int a, int b) { return a + b; } }"));
    }

    @Test
    void resetAllProgressWipesProgressAndSubmissionHistoryForTheDefaultUser() {
        assertThat(progressRepository.findByUser(user)).isNotEmpty();
        assertThat(submissionRepository.findByUserAndExerciseOrderBySubmittedAtDesc(user, exercise)).isNotEmpty();

        ResponseEntity<Void> response = controller.resetAllProgress();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(progressRepository.findByUser(user)).isEmpty();
        assertThat(submissionRepository.findByUserAndExerciseOrderBySubmittedAtDesc(user, exercise)).isEmpty();
    }

    @Test
    void getAllProgressReflectsResetImmediately() {
        assertThat(controller.getAllProgress().getBody()).isNotEmpty();

        controller.resetAllProgress();

        assertThat(controller.getAllProgress().getBody()).isEmpty();
    }
}
