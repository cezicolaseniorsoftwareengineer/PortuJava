package com.biotechpay.lab.persistence;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.ExerciseProgress;
import com.biotechpay.lab.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseProgressRepository extends JpaRepository<ExerciseProgress, Long> {
    Optional<ExerciseProgress> findByUserAndExercise(User user, Exercise exercise);
    List<ExerciseProgress> findByUser(User user);
    void deleteByUser(User user);
}
