package com.biotechpay.lab.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "exercise_id"})
})
public class ExerciseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseStatus status = ExerciseStatus.NOT_STARTED;

    private int bestPassedTests;
    private int totalTests;
    private int attemptCount;

    private LocalDateTime firstSolvedAt;
    private LocalDateTime lastAttemptAt;

    @Column(length = 4000)
    private String lastSubmittedCode;

    public ExerciseProgress() {}

    public ExerciseProgress(User user, Exercise exercise) {
        this.user = user;
        this.exercise = exercise;
        this.status = ExerciseStatus.NOT_STARTED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public ExerciseStatus getStatus() { return status; }
    public void setStatus(ExerciseStatus status) { this.status = status; }

    public int getBestPassedTests() { return bestPassedTests; }
    public void setBestPassedTests(int bestPassedTests) { this.bestPassedTests = bestPassedTests; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public LocalDateTime getFirstSolvedAt() { return firstSolvedAt; }
    public void setFirstSolvedAt(LocalDateTime firstSolvedAt) { this.firstSolvedAt = firstSolvedAt; }

    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }

    public String getLastSubmittedCode() { return lastSubmittedCode; }
    public void setLastSubmittedCode(String lastSubmittedCode) { this.lastSubmittedCode = lastSubmittedCode; }
}
