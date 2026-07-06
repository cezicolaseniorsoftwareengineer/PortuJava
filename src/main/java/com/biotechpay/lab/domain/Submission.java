package com.biotechpay.lab.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
public class Submission {

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

    @Column(nullable = false, length = 4000)
    private String submittedCode;

    private boolean compileSuccess;

    @Column(length = 4000)
    private String compileErrors;

    private int totalTests;
    private int passedTests;
    private boolean allPassed;
    private boolean executionTimedOut;

    @ElementCollection
    @CollectionTable(name = "submission_results", joinColumns = @JoinColumn(name = "submission_id"))
    @OrderColumn(name = "result_order")
    private List<TestCaseResult> results = new ArrayList<>();

    private LocalDateTime submittedAt;

    public Submission() {
        this.submittedAt = LocalDateTime.now();
    }

    public Submission(User user, Exercise exercise, String submittedCode) {
        this();
        this.user = user;
        this.exercise = exercise;
        this.submittedCode = submittedCode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public String getSubmittedCode() { return submittedCode; }
    public void setSubmittedCode(String submittedCode) { this.submittedCode = submittedCode; }

    public boolean isCompileSuccess() { return compileSuccess; }
    public void setCompileSuccess(boolean compileSuccess) { this.compileSuccess = compileSuccess; }

    public String getCompileErrors() { return compileErrors; }
    public void setCompileErrors(String compileErrors) { this.compileErrors = compileErrors; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public boolean isAllPassed() { return allPassed; }
    public void setAllPassed(boolean allPassed) { this.allPassed = allPassed; }

    public boolean isExecutionTimedOut() { return executionTimedOut; }
    public void setExecutionTimedOut(boolean executionTimedOut) { this.executionTimedOut = executionTimedOut; }

    public List<TestCaseResult> getResults() { return results; }
    public void setResults(List<TestCaseResult> results) { this.results = results; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
