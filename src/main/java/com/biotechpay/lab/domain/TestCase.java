package com.biotechpay.lab.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * A single graded check for an {@link Exercise}. setupCode/invocationExpression/expectedValueExpression
 * are literal Java source fragments, not JSON — TestHarnessGenerator splices them straight into a
 * generated harness class that JavaCodeCompiler compiles and runs alongside the student's code.
 */
@Entity
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @JsonIgnore
    private Exercise exercise;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 2000)
    private String setupCode;

    @Column(nullable = false, length = 1000)
    private String invocationExpression;

    @Column(length = 1000)
    private String expectedValueExpression;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComparisonMode comparisonMode = ComparisonMode.EQUALS;

    @Column(length = 300)
    private String expectedExceptionType;

    private int sortOrder;

    private boolean visible = true;

    public TestCase() {}

    public TestCase(Exercise exercise, String description, String setupCode, String invocationExpression,
                     String expectedValueExpression, ComparisonMode comparisonMode, int sortOrder) {
        this.exercise = exercise;
        this.description = description;
        this.setupCode = setupCode;
        this.invocationExpression = invocationExpression;
        this.expectedValueExpression = expectedValueExpression;
        this.comparisonMode = comparisonMode;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSetupCode() { return setupCode; }
    public void setSetupCode(String setupCode) { this.setupCode = setupCode; }

    public String getInvocationExpression() { return invocationExpression; }
    public void setInvocationExpression(String invocationExpression) { this.invocationExpression = invocationExpression; }

    public String getExpectedValueExpression() { return expectedValueExpression; }
    public void setExpectedValueExpression(String expectedValueExpression) { this.expectedValueExpression = expectedValueExpression; }

    public ComparisonMode getComparisonMode() { return comparisonMode; }
    public void setComparisonMode(ComparisonMode comparisonMode) { this.comparisonMode = comparisonMode; }

    public String getExpectedExceptionType() { return expectedExceptionType; }
    public void setExpectedExceptionType(String expectedExceptionType) { this.expectedExceptionType = expectedExceptionType; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
