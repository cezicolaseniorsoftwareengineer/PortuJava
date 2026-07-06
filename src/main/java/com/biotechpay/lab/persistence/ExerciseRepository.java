package com.biotechpay.lab.persistence;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    Optional<Exercise> findByExerciseId(String exerciseId);
    List<Exercise> findByModuleOrderBySortOrderAsc(LearningModule module);
    boolean existsByExerciseId(String exerciseId);
}
