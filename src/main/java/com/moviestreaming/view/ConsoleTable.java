package com.moviestreaming.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Rich ASCII Table Generator for formatted console output with auto-column sizing and truncation.
 */
public class ConsoleTable {

    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
    private int maxColumnWidth = 40;

    public ConsoleTable(String... headerNames) {
        if (headerNames != null) {
            this.headers.addAll(Arrays.asList(headerNames));
        }
    }

    public ConsoleTable setMaxColumnWidth(int maxWidth) {
        this.maxColumnWidth = Math.max(10, maxWidth);
        return this;
    }

    public ConsoleTable addRow(String... rowValues) {
        if (rowValues != null) {
            List<String> row = new ArrayList<>();
            for (String val : rowValues) {
                row.add(val != null ? val : "");
            }
            this.rows.add(row);
        }
        return this;
    }

    /**
     * Builds and returns the ASCII table as a formatted string.
     *
     * @return the formatted table string
     */
    public String render() {
        int columnCount = headers.size();
        if (columnCount == 0 && !rows.isEmpty()) {
            columnCount = rows.get(0).size();
        }
        if (columnCount == 0) {
            return "(Empty Table)";
        }

        int[] colWidths = new int[columnCount];

        // Calculate width for headers
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = Math.min(maxColumnWidth, Math.max(colWidths[i], headers.get(i).length()));
        }

        // Calculate width for rows
        for (List<String> row : rows) {
            for (int i = 0; i < row.size() && i < columnCount; i++) {
                String val = row.get(i);
                colWidths[i] = Math.min(maxColumnWidth, Math.max(colWidths[i], val.length()));
            }
        }

        StringBuilder sb = new StringBuilder();
        String horizontalSeparator = buildSeparator(colWidths);

        sb.append(horizontalSeparator).append("\n");

        // Header
        if (!headers.isEmpty()) {
            sb.append("|");
            for (int i = 0; i < columnCount; i++) {
                String header = (i < headers.size()) ? headers.get(i) : "";
                sb.append(" ").append(padRight(truncate(header, colWidths[i]), colWidths[i])).append(" |");
            }
            sb.append("\n");
            sb.append(horizontalSeparator).append("\n");
        }

        // Rows
        if (rows.isEmpty()) {
            sb.append("| ").append(padRight("(No data available)", getTotalWidth(colWidths) - 2)).append(" |\n");
        } else {
            for (List<String> row : rows) {
                sb.append("|");
                for (int i = 0; i < columnCount; i++) {
                    String val = (i < row.size()) ? row.get(i) : "";
                    sb.append(" ").append(padRight(truncate(val, colWidths[i]), colWidths[i])).append(" |");
                }
                sb.append("\n");
            }
        }

        sb.append(horizontalSeparator);
        return sb.toString();
    }

    public void print() {
        System.out.println(render());
    }

    private String buildSeparator(int[] colWidths) {
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int w : colWidths) {
            sb.append(String.join("", Collections.nCopies(w + 2, "-"))).append("+");
        }
        return sb.toString();
    }

    private int getTotalWidth(int[] colWidths) {
        int sum = 1;
        for (int w : colWidths) {
            sum += w + 3;
        }
        return sum;
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) {
            return s;
        }
        return s + String.join("", Collections.nCopies(n - s.length(), " "));
    }

    private String truncate(String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxWidth) {
            return text;
        }
        if (maxWidth <= 3) {
            return text.substring(0, maxWidth);
        }
        return text.substring(0, maxWidth - 3) + "...";
    }
}
