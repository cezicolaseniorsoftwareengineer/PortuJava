package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/** Thirty original, progressively harder interview-style programming exercises. */
@Component
public class InterviewPreparationModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public InterviewPreparationModuleSeeder(LearningModuleRepository moduleRepository,
                                             ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "technical-interview-preparation";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Entrevistas: 30 desafios HackerRank + LeetCode style",
                "ENTREVISTAS",
                "Trinta problemas originais em Básico, Médio, Intermediário e Avançado. A trilha " +
                        "cobre padrões públicos comuns em entrevistas de software e bancos: estruturas " +
                        "de dados, algoritmos, concorrência, sistemas distribuídos e integridade " +
                        "financeira. Não reproduz questões proprietárias nem garante processo seletivo.",
                9));
        synchronize(module);
        return module;
    }

    @Override
    public void synchronize(LearningModule module) {
        Stream.of(
                        InterviewBasicChallenges.build(module),
                        InterviewMediumChallenges.build(module),
                        InterviewIntermediateChallenges.build(module),
                        InterviewAdvancedChallenges.build(module))
                .flatMap(java.util.Collection::stream)
                .forEach(this::saveIfMissing);
    }

    private void saveIfMissing(Exercise exercise) {
        if (!exerciseRepository.existsByExerciseId(exercise.getExerciseId())) {
            exerciseRepository.save(exercise);
        }
    }
}
