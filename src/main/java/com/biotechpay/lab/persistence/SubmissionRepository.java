package com.biotechpay.lab.persistence;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.Submission;
import com.biotechpay.lab.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserAndExerciseOrderBySubmittedAtDesc(User user, Exercise exercise);
}
