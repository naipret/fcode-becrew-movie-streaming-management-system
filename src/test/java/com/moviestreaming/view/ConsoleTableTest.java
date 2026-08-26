package com.moviestreaming.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConsoleTable ASCII Formatter Test Suite")
class ConsoleTableTest {

    @Test
    @DisplayName("Should format ASCII table with proper borders, headers, and rows")
    void shouldRenderFormattedTable() {
        ConsoleTable table = new ConsoleTable("ID", "Title", "Rating");
        table.addRow("MOV-001", "Inception", "8.8");
        table.addRow("MOV-002", "Interstellar", "8.7");

        String rendered = table.render();

        assertThat(rendered).contains("+---------+--------------+--------+");
        assertThat(rendered).contains("| ID      | Title        | Rating |");
        assertThat(rendered).contains("| MOV-001 | Inception    | 8.8    |");
        assertThat(rendered).contains("| MOV-002 | Interstellar | 8.7    |");
    }

    @Test
    @DisplayName("Should truncate long columns exceeding maxColumnWidth")
    void shouldTruncateLongColumnValues() {
        ConsoleTable table = new ConsoleTable("Name", "Description");
        table.setMaxColumnWidth(15);
        table.addRow("Sci-Fi", "Science fiction movies and space exploration epics");

        String rendered = table.render();

        assertThat(rendered).contains("Science fict...");
    }

    @Test
    @DisplayName("Should handle empty table gracefully")
    void shouldHandleEmptyTable() {
        ConsoleTable table = new ConsoleTable("Col1", "Col2");
        String rendered = table.render();

        assertThat(rendered).contains("(No data available)");
    }
}
