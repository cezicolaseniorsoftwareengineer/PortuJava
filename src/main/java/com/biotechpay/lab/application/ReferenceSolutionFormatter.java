package com.biotechpay.lab.application;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/** Produces one canonical, syntax-aware layout for every published Java code surface. */
public final class ReferenceSolutionFormatter {

    public static final int MAX_LINE_LENGTH = 100;
    private static final int INDENT_SIZE = 4;
    private static final String LINE_SEPARATOR = "\n";
    private static final Map<String, String> FORMATTER_OPTIONS = createFormatterOptions();

    private ReferenceSolutionFormatter() {}

    /** Formats a complete Java compilation unit and rejects malformed published source. */
    public static String format(String source) {
        String normalized = normalize(source);
        validateCompilationUnit(normalized);
        TextEdit edit = createFormatter().format(
                CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
                normalized,
                0,
                normalized.length(),
                0,
                LINE_SEPARATOR);
        if (edit == null) {
            throw new IllegalArgumentException("Published Java source is not syntactically formattable");
        }
        return apply(normalized, edit);
    }

    /** Formats an annotation excerpt that may be a statement, expression or member declaration. */
    public static String formatSnippet(String source) {
        String normalized = normalize(source);
        int[] kinds = {
                CodeFormatter.K_STATEMENTS,
                CodeFormatter.K_EXPRESSION,
                CodeFormatter.K_CLASS_BODY_DECLARATIONS,
                CodeFormatter.K_UNKNOWN
        };
        for (int kind : kinds) {
            TextEdit edit = createFormatter().format(
                    kind | CodeFormatter.F_INCLUDE_COMMENTS,
                    normalized,
                    0,
                    normalized.length(),
                    0,
                    LINE_SEPARATOR);
            if (edit != null) {
                return apply(normalized, edit);
            }
        }
        throw new IllegalArgumentException("Published Java snippet is not syntactically formattable");
    }

    /**
     * Locates an explanatory excerpt inside its formatted solution and removes only the solution's
     * outer indentation. This also supports excerpts that intentionally begin mid-block.
     */
    public static String formatExcerpt(String source, String excerpt) {
        String formattedSource = format(source);
        String normalizedExcerpt = normalize(excerpt);
        CompactSource compactSource = compact(formattedSource);
        String compactExcerpt = compact(normalizedExcerpt).text();
        int compactStart = compactSource.text().indexOf(compactExcerpt);
        if (compactStart < 0) {
            throw new IllegalArgumentException("Published Java excerpt was not found in its reference solution");
        }

        int sourceStart = compactSource.offsets()[compactStart];
        int sourceEnd = compactSource.offsets()[compactStart + compactExcerpt.length() - 1] + 1;
        int lineStart = formattedSource.lastIndexOf('\n', Math.max(0, sourceStart - 1)) + 1;
        String linePrefix = formattedSource.substring(lineStart, sourceStart);
        int outerIndent = linePrefix.chars().allMatch(character -> character == ' ')
                ? linePrefix.length()
                : 0;
        String[] lines = formattedSource.substring(sourceStart, sourceEnd).split(LINE_SEPARATOR, -1);
        for (int index = 1; index < lines.length; index++) {
            lines[index] = removeOuterIndent(lines[index], outerIndent);
        }
        return String.join(LINE_SEPARATOR, lines).strip();
    }

    private static CodeFormatter createFormatter() {
        return ToolFactory.createCodeFormatter(FORMATTER_OPTIONS);
    }

    private static void validateCompilationUnit(String source) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setCompilerOptions(FORMATTER_OPTIONS);
        parser.setStatementsRecovery(false);
        parser.setBindingsRecovery(false);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        for (IProblem problem : compilationUnit.getProblems()) {
            if (problem.isError()) {
                throw new IllegalArgumentException(
                        "Published Java source is malformed at line %d: %s"
                                .formatted(problem.getSourceLineNumber(), problem.getMessage()));
            }
        }
    }

    private static Map<String, String> createFormatterOptions() {
        Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, Integer.toString(INDENT_SIZE));
        options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, Integer.toString(INDENT_SIZE));
        options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, Integer.toString(MAX_LINE_LENGTH));
        options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
        return Map.copyOf(options);
    }

    private static String normalize(String source) {
        Objects.requireNonNull(source, "source");
        String normalized = source.replace("\r\n", LINE_SEPARATOR).replace('\r', '\n').strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Published Java source must not be blank");
        }
        return normalized;
    }

    private static CompactSource compact(String source) {
        StringBuilder text = new StringBuilder(source.length());
        int[] offsets = IntStream.range(0, source.length())
                .filter(index -> !Character.isWhitespace(source.charAt(index)))
                .toArray();
        for (int offset : offsets) {
            text.append(source.charAt(offset));
        }
        return new CompactSource(text.toString(), offsets);
    }

    private static String removeOuterIndent(String line, int outerIndent) {
        int removable = 0;
        while (removable < line.length() && removable < outerIndent && line.charAt(removable) == ' ') {
            removable++;
        }
        return line.substring(removable);
    }

    private static String apply(String source, TextEdit edit) {
        Document document = new Document(source);
        try {
            edit.apply(document);
        } catch (MalformedTreeException | BadLocationException exception) {
            throw new IllegalStateException("Could not apply canonical Java formatting", exception);
        }
        return document.get().replace("\r\n", LINE_SEPARATOR).replace('\r', '\n').strip();
    }

    private record CompactSource(String text, int[] offsets) {}
}
