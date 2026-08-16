package com.moviestreaming.util;

import com.moviestreaming.config.AppConstants;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for escaping, unescaping, and tokenizing delimiter-separated values
 * supporting RFC 4180 quotation rules and custom delimiters (e.g. pipe '|').
 */
public final class CsvSanitizer {

    private static final char QUOTE_CHAR = '"';
    private static final String DOUBLE_QUOTE = "\"\"";

    private CsvSanitizer() {
        // Utility class constructor
    }

    /**
     * Escapes a single string field according to CSV/Delimiter rules.
     *
     * @param value     the raw string value
     * @param delimiter the delimiter character or string
     * @return the safely escaped string
     */
    public static String escape(String value, String delimiter) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(delimiter)
                || value.indexOf(QUOTE_CHAR) >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;

        if (!needsQuoting) {
            return value;
        }

        String escapedQuotes = value.replace("\"", DOUBLE_QUOTE);
        return QUOTE_CHAR + escapedQuotes + QUOTE_CHAR;
    }

    /**
     * Unescapes a CSV field, stripping enclosing quotes and converting double quotes back.
     *
     * @param field the raw field token from CSV
     * @return the unescaped original string
     */
    public static String unescape(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }
        String trimmed = field.trim();
        if (trimmed.length() >= 2 && trimmed.charAt(0) == QUOTE_CHAR && trimmed.charAt(trimmed.length() - 1) == QUOTE_CHAR) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            return inner.replace(DOUBLE_QUOTE, "\"");
        }
        return trimmed;
    }

    /**
     * Splits a CSV line using the default delimiter '|', respecting quoted sections.
     *
     * @param line the raw line from the CSV file
     * @return list of unescaped column values
     */
    public static List<String> split(String line) {
        return split(line, AppConstants.CSV_DELIMITER.charAt(0));
    }

    /**
     * Splits a CSV line using a custom delimiter character, respecting quoted sections.
     *
     * @param line      the raw line
     * @param delimiter the delimiter character
     * @return list of unescaped column values
     */
    public static List<String> split(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        if (line == null) {
            return tokens;
        }

        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        int length = line.length();

        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);

            if (c == QUOTE_CHAR) {
                // Check for escaped double quote ("")
                if (inQuotes && i + 1 < length && line.charAt(i + 1) == QUOTE_CHAR) {
                    currentToken.append(QUOTE_CHAR);
                    i++; // Skip the second quote
                } else {
                    inQuotes = !inQuotes;
                    currentToken.append(QUOTE_CHAR);
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(unescape(currentToken.toString()));
                currentToken.setLength(0);
            } else {
                currentToken.append(c);
            }
        }

        // Add the last token
        tokens.add(unescape(currentToken.toString()));
        return tokens;
    }

    /**
     * Joins a list of values into a single delimited line using the default delimiter '|'.
     *
     * @param values the list of values to join
     * @return the formatted CSV line
     */
    public static String join(List<String> values) {
        return join(values, AppConstants.CSV_DELIMITER);
    }

    /**
     * Joins a list of values into a single delimited line using a custom delimiter.
     *
     * @param values    the list of values to join
     * @param delimiter the delimiter string
     * @return the formatted CSV line
     */
    public static String join(List<String> values, String delimiter) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(escape(values.get(i), delimiter));
        }
        return sb.toString();
    }
}
