package com.biotechpay.lab.seed.support;

import com.biotechpay.lab.application.ReferenceSolutionFormatter;
import com.biotechpay.lab.domain.ComparisonMode;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.domain.SolutionAnnotation;
import com.biotechpay.lab.domain.TestCase;

/**
 * Fluent builder for seeding an Exercise plus its ordered TestCases and hints - replaces the old
 * DataSeeder's lesson()/stepFull() free functions with something that reads at the call site.
 */
public class ExerciseBuilder {

    private final Exercise exercise;
    private int testCaseSortOrder = 0;

    private ExerciseBuilder(Exercise exercise) {
        this.exercise = exercise;
    }

    public static ExerciseBuilder of(String exerciseId, LearningModule module, String title,
                                      String statementMarkdown, String codeContract, String starterCode,
                                      String difficulty, int sortOrder, int estimatedMinutes) {
        Exercise exercise = new Exercise(exerciseId, module, title, statementMarkdown, codeContract,
                starterCode, difficulty, sortOrder, estimatedMinutes);
        return new ExerciseBuilder(exercise);
    }

    public ExerciseBuilder referenceSolution(String code) {
        exercise.setReferenceSolution(ReferenceSolutionFormatter.format(code));
        return this;
    }

    /**
     * Pairs a short excerpt of the referenceSolution with why it's written that way - surfaced
     * alongside the full solution once a student explicitly reveals it, so the answer explains
     * itself instead of just being a wall of code to copy.
     */
    public ExerciseBuilder solutionAnnotation(String codeExcerpt, String explanation) {
        exercise.getSolutionAnnotations().add(new SolutionAnnotation(codeExcerpt, explanation));
        return this;
    }

    public ExerciseBuilder equalsCase(String description, String setupCode, String invocationExpression,
                                       String expectedValueExpression, boolean visible) {
        return addCase(description, setupCode, invocationExpression, expectedValueExpression,
                ComparisonMode.EQUALS, null, visible);
    }

    public ExerciseBuilder throwsCase(String description, String setupCode, String invocationExpression,
                                       String expectedExceptionType, boolean visible) {
        return addCase(description, setupCode, invocationExpression, null,
                ComparisonMode.THROWS, expectedExceptionType, visible);
    }

    public ExerciseBuilder stdoutContainsCase(String description, String setupCode, String invocationExpression,
                                                String expectedSubstringExpression, boolean visible) {
        return addCase(description, setupCode, invocationExpression, expectedSubstringExpression,
                ComparisonMode.STDOUT_CONTAINS, null, visible);
    }

    public ExerciseBuilder exceptionMessageContainsCase(String description, String setupCode, String invocationExpression,
                                                          String expectedExceptionType, String expectedSubstringExpression,
                                                          boolean visible) {
        return addCase(description, setupCode, invocationExpression, expectedSubstringExpression,
                ComparisonMode.EXCEPTION_MESSAGE_CONTAINS, expectedExceptionType, visible);
    }

    private ExerciseBuilder addCase(String description, String setupCode, String invocationExpression,
                                     String expectedValueExpression, ComparisonMode mode,
                                     String expectedExceptionType, boolean visible) {
        TestCase testCase = new TestCase(exercise, description, setupCode, invocationExpression,
                expectedValueExpression, mode, testCaseSortOrder++);
        testCase.setExpectedExceptionType(expectedExceptionType);
        testCase.setVisible(visible);
        exercise.getTestCases().add(testCase);
        return this;
    }

    public ExerciseBuilder hint(String text) {
        exercise.getHints().add(text);
        return this;
    }

    public Exercise build() {
        return exercise;
    }
}
