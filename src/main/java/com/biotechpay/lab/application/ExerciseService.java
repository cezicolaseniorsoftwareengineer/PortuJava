package com.biotechpay.lab.application;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.ExerciseProgress;
import com.biotechpay.lab.domain.ExerciseStatus;
import com.biotechpay.lab.domain.User;
import com.biotechpay.lab.persistence.ExerciseProgressRepository;
import com.biotechpay.lab.persistence.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read side for a single exercise. The detail view intentionally omits TestCase content and
 * referenceSolution; hidden test cases never leave the server (see SubmissionService for the grading
 * path). The reference solution is exposed only through the explicit reveal endpoint
 * ({@link #getSolution}) - a deliberate product choice so the student opts in to the spoiler instead
 * of receiving it embedded in the exercise payload.
 */
@Service
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseProgressRepository progressRepository;
    private final UserService userService;

    public ExerciseService(ExerciseRepository exerciseRepository,
                            ExerciseProgressRepository progressRepository,
                            UserService userService) {
        this.exerciseRepository = exerciseRepository;
        this.progressRepository = progressRepository;
        this.userService = userService;
    }

    public ExerciseDetail getExerciseDetail(String exerciseId) {
        Exercise exercise = findExercise(exerciseId);
        User user = userService.getOrCreateDefaultUser();
        ExerciseProgress progress = progressRepository.findByUserAndExercise(user, exercise).orElse(null);

        String editorCode = progress != null && progress.getLastSubmittedCode() != null
                ? progress.getLastSubmittedCode()
                : exercise.getStarterCode();
        ExerciseStatus status = progress != null ? progress.getStatus() : ExerciseStatus.NOT_STARTED;

        return new ExerciseDetail(
                exercise.getExerciseId(),
                exercise.getTitle(),
                exercise.getStatementMarkdown(),
                exercise.getCodeContract(),
                editorCode,
                exercise.getDifficulty(),
                exercise.getEstimatedMinutes(),
                exercise.getModule().getModuleCode(),
                exercise.getModule().getTitle(),
                exercise.getHints().size(),
                status);
    }

    public SolutionView getSolution(String exerciseId) {
        Exercise exercise = findExercise(exerciseId);
        if (exercise.getReferenceSolution() == null || exercise.getReferenceSolution().isBlank()) {
            throw new IllegalStateException("Exercicio sem solucao de referencia: " + exerciseId);
        }
        List<AnnotationView> annotations = exercise.getSolutionAnnotations().stream()
                .map(a -> new AnnotationView(a.getCodeExcerpt(), a.getExplanation()))
                .toList();
        return new SolutionView(ReferenceSolutionFormatter.format(exercise.getReferenceSolution()),
                List.copyOf(exercise.getHints()), annotations);
    }

    public HintView getHint(String exerciseId, int index) {
        Exercise exercise = findExercise(exerciseId);
        List<String> hints = exercise.getHints();
        if (index < 0 || index >= hints.size()) {
            throw new IllegalArgumentException("Indice de dica invalido: " + index);
        }
        return new HintView(index, hints.get(index), index < hints.size() - 1);
    }

    private Exercise findExercise(String exerciseId) {
        return exerciseRepository.findByExerciseId(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercício não encontrado: " + exerciseId));
    }

    public record ExerciseDetail(
            String exerciseId,
            String title,
            String statementMarkdown,
            String codeContract,
            String editorCode,
            String difficulty,
            int estimatedMinutes,
            String moduleCode,
            String moduleTitle,
            int hintCount,
            ExerciseStatus status
    ) {}

    public record HintView(int index, String text, boolean hasMore) {}

    /**
     * Full reveal: the reference solution, the walkthrough steps (the exercise's hints), and
     * annotations pairing a code excerpt with why it's written that way.
     */
    public record SolutionView(String solutionCode, List<String> steps, List<AnnotationView> annotations) {}

    public record AnnotationView(String codeExcerpt, String explanation) {}
}
