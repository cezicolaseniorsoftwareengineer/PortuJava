package com.biotechpay.lab.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Produces a stable, copyable representation for published Java reference solutions.
 *
 * <p>The formatter is intentionally conservative: it normalizes whitespace and only inserts line
 * breaks at lexical whitespace, after commas or before member-access dots outside string and
 * character literals. It never rewrites identifiers, operators, literals or control flow.</p>
 */
public final class ReferenceSolutionFormatter {

    public static final int MAX_LINE_LENGTH = 100;
    private static final int CONTINUATION_INDENT = 4;

    private ReferenceSolutionFormatter() {}

    public static String format(String source) {
        Objects.requireNonNull(source, "source");
        String normalized = source.replace("\r\n", "\n").replace('\r', '\n');
        String[] rawLines = normalized.split("\n", -1);
        int first = 0;
        while (first < rawLines.length && rawLines[first].isBlank()) {
            first++;
        }
        int last = rawLines.length - 1;
        while (last >= first && rawLines[last].isBlank()) {
            last--;
        }

        List<String> formatted = new ArrayList<>();
        boolean previousBlank = false;
        for (int index = first; index <= last; index++) {
            String line = rawLines[index].replace("\t", "    ").stripTrailing();
            if (line.isBlank()) {
                if (!previousBlank) {
                    formatted.add("");
                }
                previousBlank = true;
                continue;
            }
            formatted.addAll(wrap(line));
            previousBlank = false;
        }
        return String.join("\n", formatted);
    }

    private static List<String> wrap(String originalLine) {
        List<String> lines = new ArrayList<>();
        String remaining = originalLine;
        int baseIndent = leadingSpaces(originalLine);
        int continuationIndent = baseIndent + CONTINUATION_INDENT;

        while (remaining.length() > MAX_LINE_LENGTH) {
            int breakAt = lastSafeBreakAtOrBefore(remaining, MAX_LINE_LENGTH);
            if (breakAt <= leadingSpaces(remaining)) {
                break;
            }
            String head = remaining.substring(0, breakAt).stripTrailing();
            String tail = remaining.substring(breakAt).stripLeading();
            if (head.isEmpty() || tail.isEmpty()) {
                break;
            }
            lines.add(head);
            remaining = " ".repeat(continuationIndent) + tail;
        }
        lines.add(remaining);
        return lines;
    }

    private static int lastSafeBreakAtOrBefore(String line, int limit) {
        boolean inString = false;
        boolean inCharacter = false;
        boolean escaped = false;
        int candidate = -1;
        int minimumContent = leadingSpaces(line) + 12;

        for (int index = 0; index < line.length() && index <= limit; index++) {
            char current = line.charAt(index);
            char next = index + 1 < line.length() ? line.charAt(index + 1) : '\0';

            if (escaped) {
                escaped = false;
                continue;
            }
            if ((inString || inCharacter) && current == '\\') {
                escaped = true;
                continue;
            }
            if (!inCharacter && current == '"') {
                inString = !inString;
                continue;
            }
            if (!inString && current == '\'') {
                inCharacter = !inCharacter;
                continue;
            }
            if (inString || inCharacter) {
                continue;
            }
            if (current == '/' && next == '/') {
                break;
            }
            if (index >= minimumContent && Character.isWhitespace(current)) {
                candidate = index;
            } else if (index >= minimumContent && current == ',') {
                candidate = index + 1;
            } else if (index >= minimumContent && current == '.' && !isDecimalPoint(line, index)) {
                candidate = index;
            }
        }
        return candidate;
    }

    private static boolean isDecimalPoint(String line, int index) {
        return index > 0
                && index + 1 < line.length()
                && Character.isDigit(line.charAt(index - 1))
                && Character.isDigit(line.charAt(index + 1));
    }

    private static int leadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }
}
