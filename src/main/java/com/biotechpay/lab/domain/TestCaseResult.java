package com.biotechpay.lab.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Per-test grading outcome attached to a {@link Submission}. actualValueSummary/exceptionSummary are
 * only ever populated for visible test cases so a failing hidden test never leaks what it checked.
 */
@Embeddable
public class TestCaseResult {

    @Column(name = "result_test_case_id")
    private Long testCaseId;

    @Column(name = "result_description", length = 1000)
    private String description;

    @Column(name = "result_passed")
    private boolean passed;

    @Column(name = "result_hidden")
    private boolean hidden;

    @Column(name = "result_actual_value", length = 1000)
    private String actualValueSummary;

    @Column(name = "result_exception", length = 1000)
    private String exceptionSummary;

    public TestCaseResult() {}

    public TestCaseResult(Long testCaseId, String description, boolean passed, boolean hidden,
                           String actualValueSummary, String exceptionSummary) {
        this.testCaseId = testCaseId;
        this.description = description;
        this.passed = passed;
        this.hidden = hidden;
        this.actualValueSummary = actualValueSummary;
        this.exceptionSummary = exceptionSummary;
    }

    public Long getTestCaseId() { return testCaseId; }
    public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public String getActualValueSummary() { return actualValueSummary; }
    public void setActualValueSummary(String actualValueSummary) { this.actualValueSummary = actualValueSummary; }

    public String getExceptionSummary() { return exceptionSummary; }
    public void setExceptionSummary(String exceptionSummary) { this.exceptionSummary = exceptionSummary; }
}
