package com.biotechpay.lab.application;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.ExerciseProgress;
import com.biotechpay.lab.domain.ExerciseStatus;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.domain.User;
import com.biotechpay.lab.persistence.ExerciseProgressRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read side for the module/exercise tracks. Never exposes TestCase or referenceSolution content -
 * that stays server-side only, reachable exclusively through SubmissionService's grading path.
 */
@Service
@Transactional(readOnly = true)
public class ModuleService {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseProgressRepository progressRepository;
    private final UserService userService;

    public ModuleService(LearningModuleRepository moduleRepository,
                          ExerciseProgressRepository progressRepository,
                          UserService userService) {
        this.moduleRepository = moduleRepository;
        this.progressRepository = progressRepository;
        this.userService = userService;
    }

    public List<ModuleSummary> getAllModules() {
        User user = userService.getOrCreateDefaultUser();
        Map<Long, ExerciseProgress> progressByExerciseId = progressByExerciseId(user);
        return moduleRepository.findAllByOrderBySortOrderAsc().stream()
                .map(module -> toSummary(module, progressByExerciseId))
                .toList();
    }

    public ModuleSummary getModule(String moduleCode) {
        LearningModule module = moduleRepository.findByModuleCode(moduleCode)
                .orElseThrow(() -> new IllegalArgumentException("Módulo não encontrado: " + moduleCode));
        User user = userService.getOrCreateDefaultUser();
        return toSummary(module, progressByExerciseId(user));
    }

    private Map<Long, ExerciseProgress> progressByExerciseId(User user) {
        Map<Long, ExerciseProgress> byExerciseId = new HashMap<>();
        for (ExerciseProgress progress : progressRepository.findByUser(user)) {
            byExerciseId.put(progress.getExercise().getId(), progress);
        }
        return byExerciseId;
    }

    private ModuleSummary toSummary(LearningModule module, Map<Long, ExerciseProgress> progressByExerciseId) {
        List<ExerciseSummary> exercises = module.getExercises().stream()
                .map(exercise -> toExerciseSummary(exercise, progressByExerciseId.get(exercise.getId())))
                .toList();
        long solvedCount = exercises.stream().filter(e -> e.status() == ExerciseStatus.SOLVED).count();
        return new ModuleSummary(module.getModuleCode(), module.getTitle(), module.getParadigm(),
                module.getDescription(), module.getSortOrder(), exercises, (int) solvedCount, exercises.size());
    }

    private ExerciseSummary toExerciseSummary(Exercise exercise, ExerciseProgress progress) {
        ExerciseStatus status = progress != null ? progress.getStatus() : ExerciseStatus.NOT_STARTED;
        return new ExerciseSummary(exercise.getExerciseId(), exercise.getTitle(), exercise.getDifficulty(),
                exercise.getEstimatedMinutes(), exercise.getSortOrder(), status);
    }

    public record ExerciseSummary(String exerciseId, String title, String difficulty, int estimatedMinutes,
                                    int sortOrder, ExerciseStatus status) {}

    public record ModuleSummary(String moduleCode, String title, String paradigm, String description,
                                  int sortOrder, List<ExerciseSummary> exercises, int solvedCount, int totalCount) {}
}
