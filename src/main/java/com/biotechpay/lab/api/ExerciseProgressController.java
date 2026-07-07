package com.biotechpay.lab.api;

import com.biotechpay.lab.application.UserService;
import com.biotechpay.lab.domain.ExerciseProgress;
import com.biotechpay.lab.domain.ExerciseStatus;
import com.biotechpay.lab.domain.User;
import com.biotechpay.lab.persistence.ExerciseProgressRepository;
import com.biotechpay.lab.persistence.SubmissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only progress for the default local user across the new exercise-based model. Kept as a
 * separate path/class from the legacy /api/progress (ProgressController, typing-game era) so both can
 * coexist until the old model is retired.
 */
@RestController
@RequestMapping("/api/exercise-progress")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
@Transactional(readOnly = true)
public class ExerciseProgressController {

    private final ExerciseProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    public ExerciseProgressController(ExerciseProgressRepository progressRepository,
                                       SubmissionRepository submissionRepository,
                                       UserService userService) {
        this.progressRepository = progressRepository;
        this.submissionRepository = submissionRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ProgressEntry>> getAllProgress() {
        User user = userService.getOrCreateDefaultUser();
        List<ProgressEntry> entries = progressRepository.findByUser(user).stream()
                .map(ExerciseProgressController::toEntry)
                .toList();
        return ResponseEntity.ok(entries);
    }

    /**
     * Resets every exercise back to NOT_STARTED and wipes submission history for the default user -
     * a deliberate "start over" action for a single-user practice tool, not a soft/per-exercise undo.
     */
    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> resetAllProgress() {
        User user = userService.getOrCreateDefaultUser();
        progressRepository.deleteByUser(user);
        submissionRepository.deleteByUser(user);
        return ResponseEntity.noContent().build();
    }

    private static ProgressEntry toEntry(ExerciseProgress progress) {
        return new ProgressEntry(
                progress.getExercise().getExerciseId(),
                progress.getStatus(),
                progress.getBestPassedTests(),
                progress.getTotalTests(),
                progress.getAttemptCount(),
                progress.getFirstSolvedAt(),
                progress.getLastAttemptAt());
    }

    public record ProgressEntry(
            String exerciseId,
            ExerciseStatus status,
            int bestPassedTests,
            int totalTests,
            int attemptCount,
            LocalDateTime firstSolvedAt,
            LocalDateTime lastAttemptAt
    ) {}
}
