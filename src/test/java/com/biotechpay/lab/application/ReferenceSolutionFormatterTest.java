package com.biotechpay.lab.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceSolutionFormatterTest {

    @Test
    void formatsCompleteSourceUsingJavaSyntaxAndFourSpaceIndentation() {
        String source = "\r\n\tpublic final class EngineeringGate01 {  \r\n" +
                "\tprivate static final java.util.List<String> REQUIRED = " +
                "java.util.List.of(\"problemObserved\", \"sourceOfTruthLocated\", " +
                "\"invariantsWritten\", \"successCriteriaMeasurable\");\r\n\r\n\r\n" +
                "\tpublic boolean evaluate() { return true; }\r\n}\r\n";

        String formatted = ReferenceSolutionFormatter.format(source);

        assertThat(formatted)
                .doesNotContain("\r", "\t", "  \n", "\n\n\n")
                .isEqualTo("""
                        public final class EngineeringGate01 {
                            private static final java.util.List<String> REQUIRED = java.util.List.of("problemObserved",
                                    "sourceOfTruthLocated", "invariantsWritten", "successCriteriaMeasurable");

                            public boolean evaluate() {
                                return true;
                            }
                        }
                        """.strip());
        assertThat(formatted.lines()).allSatisfy(line ->
                assertThat(line.length()).isLessThanOrEqualTo(ReferenceSolutionFormatter.MAX_LINE_LENGTH));
        assertThat(formatted).contains("\"sourceOfTruthLocated\"");
    }

    @Test
    void neverBreaksInsideStringOrCharacterLiterals() {
        String longLiteral = "x".repeat(130);
        String source = "public class Literal {\n" +
                "    String value = \"" + longLiteral + "\";\n" +
                "    char quote = '\"';\n" +
                "}";

        String formatted = ReferenceSolutionFormatter.format(source);

        assertThat(formatted).contains("\"" + longLiteral + "\"");
        assertThat(formatted).contains("char quote = '\"';");
    }

    @Test
    void wrapsFluentCallsBeforeMemberAccessWithoutSplittingDecimalLiterals() {
        String source = "public final class Example {\n" +
                "public String merge(int[] merged) { return java.util.Arrays.stream(merged)" +
                ".mapToObj(String::valueOf).collect(java.util.stream.Collectors.joining(\",\")); }\n" +
                "double threshold = 123456789012345678901234567890123456789012345678901234567890.125;\n" +
                "}";

        String formatted = ReferenceSolutionFormatter.format(source);

        assertThat(formatted).contains("\n                .collect(");
        assertThat(formatted).contains(".125;");
        assertThat(formatted.lines()).allSatisfy(line ->
                assertThat(line.length()).isLessThanOrEqualTo(ReferenceSolutionFormatter.MAX_LINE_LENGTH));
    }

    @Test
    void formatsStatementAndExpressionExcerptsWithoutInventingAnOuterClass() {
        assertThat(ReferenceSolutionFormatter.formatSnippet("if (ready) { return value; }"))
                .isEqualTo("""
                        if (ready) {
                            return value;
                        }
                        """.strip());
        assertThat(ReferenceSolutionFormatter.formatSnippet("missing.isEmpty()&&active.isEmpty()"))
                .isEqualTo("missing.isEmpty() && active.isEmpty()");
    }

    @Test
    void derivesPartialBlockExcerptFromTheFormattedCompleteSolution() {
        String source = """
                public final class Machine {
                    public String brew(String type) {
                        if ("ESPRESSO".equals(type)) {
                            return "BREWING";
                        } else {
                            return "UNKNOWN_DRINK";
                        }
                    }
                }
                """;

        assertThat(ReferenceSolutionFormatter.formatExcerpt(source, """
                } else {
                        return "UNKNOWN_DRINK";
                    }
                """))
                .isEqualTo("""
                        } else {
                            return "UNKNOWN_DRINK";
                        }
                        """.strip());
    }

    @Test
    void keepsEveryContinuationIndentedInTheReportedEngineeringGateShape() {
        String source = """
                public final class EngineeringGate01 {
                    private static final java.util.List<String> REQUIRED = java.util.List.of("problemObserved", "sourceOfTruthLocated", "invariantsWritten", "successCriteriaMeasurable");
                    private static final java.util.List<String> VETOES = java.util.List.of("assumptionPresentedAsFact", "scopeNotAuthorized");
                    public record Decision(boolean released, java.util.List<String> missing,
                                           java.util.List<String> vetoes) {}
                    public Decision evaluate(java.util.Map<String, Boolean> evidence) {
                        if (evidence == null) throw new IllegalArgumentException("Evidence is required");
                        java.util.List<String> missing = REQUIRED.stream()
                                .filter(key -> !Boolean.TRUE.equals(evidence.get(key))).toList();
                        java.util.List<String> active = VETOES.stream()
                                .filter(key -> Boolean.TRUE.equals(evidence.get(key))).toList();
                        return new Decision(missing.isEmpty() && active.isEmpty(), missing, active);
                    }
                }
                """;

        String formatted = ReferenceSolutionFormatter.format(source);

        assertThat(formatted).startsWith("public final class EngineeringGate01 {");
        assertThat(formatted).doesNotContain("\nmissing,", "\nvetoes)", "\nactive);");
        assertThat(formatted.lines())
                .filteredOn(line -> !line.isBlank())
                .allSatisfy(line -> {
                    int indentation = line.length() - line.stripLeading().length();
                    assertThat(indentation % 4).as("four-space indentation for <%s>", line).isZero();
                });
        assertThat(formatted.lines())
                .allSatisfy(line -> assertThat(line.length())
                        .isLessThanOrEqualTo(ReferenceSolutionFormatter.MAX_LINE_LENGTH));
    }

    @Test
    void rejectsMalformedPublishedCompilationUnits() {
        assertThatThrownBy(() -> ReferenceSolutionFormatter.format("public class Broken {"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("malformed");
    }
}
