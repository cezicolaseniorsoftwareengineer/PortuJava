package com.biotechpay.lab.application;

import com.biotechpay.lab.domain.ComparisonMode;
import com.biotechpay.lab.domain.TestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestHarnessGeneratorTest {

    private final TestHarnessGenerator generator = new TestHarnessGenerator();
    private final JavaCodeCompiler compiler = new JavaCodeCompiler();

    private static final String STUDENT_SOURCE = """
            public class Solution {
                private int water;

                public Solution(int water) {
                    if (water < 0) {
                        throw new IllegalArgumentException("water must not be negative");
                    }
                    this.water = water;
                }

                public int getWater() {
                    return water;
                }

                public void printGreeting() {
                    System.out.println("hello from invocation");
                }
            }
            """;

    @Test
    void equalsModeReportsPassAndFailWithActualValue() {
        TestCase passingCase = testCase("ok", "Solution s = new Solution(10);", "s.getWater()", "10", ComparisonMode.EQUALS, 0);
        TestCase failingCase = testCase("mismatch", "Solution s = new Solution(10);", "s.getWater()", "99", ComparisonMode.EQUALS, 1);

        String output = runHarness(List.of(passingCase, failingCase));

        assertThat(output).contains("RESULT#0#PASS");
        assertThat(output).contains("RESULT#1#FAIL#10");
    }

    @Test
    void throwsModeDistinguishesCorrectExceptionTypeFromWrongType() {
        TestCase correctType = new TestCase(null, "throws-correct", "", "new Solution(-1)", null, ComparisonMode.THROWS, 0);
        correctType.setExpectedExceptionType("java.lang.IllegalArgumentException");

        TestCase wrongType = new TestCase(null, "throws-wrong", "", "new Solution(-1)", null, ComparisonMode.THROWS, 1);
        wrongType.setExpectedExceptionType("java.lang.NullPointerException");

        TestCase noThrow = new TestCase(null, "throws-none", "", "new Solution(5)", null, ComparisonMode.THROWS, 2);
        noThrow.setExpectedExceptionType("java.lang.IllegalArgumentException");

        String output = runHarness(List.of(correctType, wrongType, noThrow));

        assertThat(output).contains("RESULT#0#PASS");
        assertThat(output).contains("RESULT#1#FAIL#java.lang.IllegalArgumentException");
        assertThat(output).contains("RESULT#2#FAIL#nenhuma exceção foi lançada");
    }

    @Test
    void stdoutContainsCapturesOnlyTheInvocationNotTheSetupCode() {
        TestCase testCase = testCase(
                "stdout-scope",
                "System.out.println(\"from setup, should not be captured\"); Solution s = new Solution(1);",
                "s.printGreeting()",
                "\"hello\"",
                ComparisonMode.STDOUT_CONTAINS,
                0);

        String output = runHarness(List.of(testCase));

        assertThat(output).contains("RESULT#0#PASS");
        assertThat(output).contains("from setup, should not be captured");
    }

    @Test
    void generatedFailureMessagesNeverEchoTheExpectedValue() {
        // A hidden test case's expected value must never leak through the FAIL branch, only the
        // actual value the student's code produced - this is what SubmissionService relies on when
        // deciding what to forward to the client for hidden test cases. The expected-value literal
        // necessarily appears once, as the right-hand side of the "expected" assignment - what must
        // never happen is the FAIL println referencing the "expected" variable itself.
        TestCase failingCase = testCase("secret-check", "Solution s = new Solution(10);", "s.getWater()", "\"top-secret-value\"", ComparisonMode.EQUALS, 0);

        String source = generator.generate("Harness", List.of(failingCase));

        assertThat(source).contains("Object expected = \"top-secret-value\";");
        assertThat(source).doesNotContain("FAIL#\" + expected");
        assertThat(source).doesNotContain("FAIL#\" + describe(expected)");
    }

    private String runHarness(List<TestCase> testCases) {
        String harnessSource = generator.generate("Harness", testCases);
        JavaCodeCompiler.CompileAndRunResult result = compiler.compileAndRunHarness(STUDENT_SOURCE, harnessSource, "Solution", "Harness");
        assertThat(result.errors()).as("harness compile errors").isEmpty();
        return result.output();
    }

    private TestCase testCase(String description, String setupCode, String invocationExpression,
                               String expectedValueExpression, ComparisonMode mode, int sortOrder) {
        return new TestCase(null, description, setupCode, invocationExpression, expectedValueExpression, mode, sortOrder);
    }
}
