package com.biotechpay.lab.application;

import com.biotechpay.lab.domain.TestCase;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Turns a list of {@link TestCase}s into a single Java source file: a generated {@code main} class
 * that drives the student's compiled code and prints one machine-readable {@code RESULT#<index>#...}
 * marker line per test case, which {@code SubmissionService} parses back into pass/fail results.
 *
 * <p>setupCode/invocationExpression/expectedValueExpression are spliced verbatim as Java source — see
 * {@link TestCase} for why literal expressions were chosen over a JSON+reflection marshaller. Test
 * case authors are responsible for writing valid Java there; a bad expression surfaces as a harness
 * compile failure, which {@code SubmissionService} treats as an internal grading error rather than
 * showing the raw diagnostic to the student (it could echo the hidden expected-value expression back).
 */
@Service
public class TestHarnessGenerator {

    public String generate(String harnessClassName, List<TestCase> testCases) {
        StringBuilder src = new StringBuilder();
        src.append("public class ").append(harnessClassName).append(" {\n");
        src.append("    public static void main(String[] args) throws Exception {\n");

        int index = 0;
        for (TestCase testCase : testCases) {
            src.append(renderTestCase(index, testCase));
            index++;
        }

        src.append("    }\n");
        src.append(DESCRIBE_HELPER);
        src.append("}\n");
        return src.toString();
    }

    /**
     * Renders a checked value into a single-line, length-capped string safe to embed in a RESULT#
     * marker line — collapses embedded newlines (which would otherwise break the line-based parser
     * in SubmissionService) and caps length so a runaway toString() can't flood the marker output.
     */
    private static final String DESCRIBE_HELPER = """
                private static String describe(Object value) {
                    String text = String.valueOf(value).replace("\\r", " ").replace("\\n", " ");
                    return text.length() > 500 ? text.substring(0, 500) + "..." : text;
                }
            """;

    private String renderTestCase(int index, TestCase testCase) {
        String setup = blankToEmpty(testCase.getSetupCode());
        String invocation = testCase.getInvocationExpression();

        return switch (testCase.getComparisonMode()) {
            case EQUALS -> renderEquals(index, setup, invocation, testCase.getExpectedValueExpression());
            case THROWS -> renderThrows(index, setup, invocation, testCase.getExpectedExceptionType());
            case STDOUT_CONTAINS -> renderStdout(index, setup, invocation, testCase.getExpectedValueExpression(), true);
            case STDOUT_EQUALS -> renderStdout(index, setup, invocation, testCase.getExpectedValueExpression(), false);
            case EXCEPTION_MESSAGE_CONTAINS -> renderExceptionMessage(index, setup, invocation, testCase.getExpectedExceptionType(), testCase.getExpectedValueExpression());
        };
    }

    private String renderEquals(int index, String setup, String invocation, String expectedExpression) {
        return """
                        try {
                            %s
                            Object actual = %s;
                            Object expected = %s;
                            if (java.util.Objects.deepEquals(actual, expected)) {
                                System.out.println("RESULT#%d#PASS");
                            } else {
                                System.out.println("RESULT#%d#FAIL#" + describe(actual));
                            }
                        } catch (Throwable t) {
                            System.out.println("RESULT#%d#EXCEPTION#" + t.getClass().getName() + "#" + describe(t.getMessage()));
                        }
                """.formatted(setup, invocation, expectedExpression, index, index, index);
    }

    private String renderThrows(int index, String setup, String invocation, String expectedExceptionType) {
        String exceptionType = blankToEmpty(expectedExceptionType).isBlank() ? "Throwable" : expectedExceptionType;
        return """
                        try {
                            %s
                            %s;
                            System.out.println("RESULT#%d#FAIL#nenhuma excecao foi lancada");
                        } catch (%s t) {
                            System.out.println("RESULT#%d#PASS");
                        } catch (Throwable t) {
                            System.out.println("RESULT#%d#FAIL#" + t.getClass().getName());
                        }
                """.formatted(setup, invocation, index, exceptionType, index, index);
    }

    private String renderExceptionMessage(int index, String setup, String invocation, String expectedExceptionType, String expectedSubstringExpression) {
        String exceptionType = blankToEmpty(expectedExceptionType).isBlank() ? "Throwable" : expectedExceptionType;
        return """
                        try {
                            %s
                            %s;
                            System.out.println("RESULT#%d#FAIL#nenhuma excecao foi lancada");
                        } catch (%s t) {
                            String expectedSubstring = %s;
                            String actualMessage = t.getMessage() == null ? "" : t.getMessage();
                            if (actualMessage.contains(expectedSubstring)) {
                                System.out.println("RESULT#%d#PASS");
                            } else {
                                System.out.println("RESULT#%d#FAIL#" + describe(actualMessage));
                            }
                        } catch (Throwable t) {
                            System.out.println("RESULT#%d#FAIL#" + t.getClass().getName());
                        }
                """.formatted(setup, invocation, index, exceptionType, expectedSubstringExpression, index, index, index);
    }

    private String renderStdout(int index, String setup, String invocation, String expectedExpression, boolean containsMode) {
        String comparison = containsMode
                ? "captured.contains(expected)"
                : "captured.trim().equals(expected.trim())";
        return """
                        try {
                            %2$s
                            java.io.ByteArrayOutputStream harnessBuffer%1$d = new java.io.ByteArrayOutputStream();
                            java.io.PrintStream harnessOriginalOut%1$d = System.out;
                            try {
                                System.setOut(new java.io.PrintStream(harnessBuffer%1$d, true, java.nio.charset.StandardCharsets.UTF_8));
                                %3$s;
                            } finally {
                                System.setOut(harnessOriginalOut%1$d);
                            }
                            String captured = harnessBuffer%1$d.toString(java.nio.charset.StandardCharsets.UTF_8);
                            String expected = %4$s;
                            if (%5$s) {
                                System.out.println("RESULT#%1$d#PASS");
                            } else {
                                System.out.println("RESULT#%1$d#FAIL#" + describe(captured));
                            }
                        } catch (Throwable t) {
                            System.out.println("RESULT#%1$d#EXCEPTION#" + t.getClass().getName() + "#" + describe(t.getMessage()));
                        }
                """.formatted(index, setup, invocation, expectedExpression, comparison);
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
