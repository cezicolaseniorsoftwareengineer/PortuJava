package com.biotechpay.lab.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceSolutionFormatterTest {

    @Test
    void normalizesWhitespaceAndWrapsOnlyAtSafeJavaBoundaries() {
        String source = "\r\n\tpublic final class EngineeringGate01 {  \r\n" +
                "\tprivate static final java.util.List<String> REQUIRED = " +
                "java.util.List.of(\"problemObserved\", \"sourceOfTruthLocated\", " +
                "\"invariantsWritten\", \"successCriteriaMeasurable\");\r\n\r\n\r\n" +
                "\tpublic boolean evaluate() { return true; }\r\n}\r\n";

        String formatted = ReferenceSolutionFormatter.format(source);

        assertThat(formatted)
                .doesNotContain("\r", "\t", "  \n", "\n\n\n")
                .startsWith("    public final class EngineeringGate01 {")
                .endsWith("}");
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
        String source = "        return java.util.Arrays.stream(merged).mapToObj(String::valueOf)" +
                ".collect(java.util.stream.Collectors.joining(\",\"));\n" +
                "        double threshold = 123456789012345678901234567890123456789012345678901234567890" +
                ".125;";

        String formatted = ReferenceSolutionFormatter.format(source);

        assertThat(formatted).contains("\n            .Collectors.joining(");
        assertThat(formatted).contains(".125;");
        assertThat(formatted.lines()).allSatisfy(line ->
                assertThat(line.length()).isLessThanOrEqualTo(ReferenceSolutionFormatter.MAX_LINE_LENGTH));
    }
}
