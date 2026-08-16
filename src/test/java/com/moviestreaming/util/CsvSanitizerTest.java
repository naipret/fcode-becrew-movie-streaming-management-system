package com.moviestreaming.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CsvSanitizer Utility Test Suite")
class CsvSanitizerTest {

    @Test
    @DisplayName("Should escape values containing pipe delimiter")
    void shouldEscapeValuesWithDelimiter() {
        String input = "Action | Adventure";
        String escaped = CsvSanitizer.escape(input, "|");
        assertThat(escaped).isEqualTo("\"Action | Adventure\"");
    }

    @Test
    @DisplayName("Should escape values containing quotes")
    void shouldEscapeValuesWithQuotes() {
        String input = "The \"Matrix\" Reloaded";
        String escaped = CsvSanitizer.escape(input, "|");
        assertThat(escaped).isEqualTo("\"The \"\"Matrix\"\" Reloaded\"");
    }

    @Test
    @DisplayName("Should return normal value if no special characters exist")
    void shouldNotEscapePlainValues() {
        String input = "Inception";
        String escaped = CsvSanitizer.escape(input, "|");
        assertThat(escaped).isEqualTo("Inception");
    }

    @Test
    @DisplayName("Should unescape quoted values with double-quotes correctly")
    void shouldUnescapeQuotedValues() {
        String input = "\"The \"\"Dark\"\" Knight\"";
        String unescaped = CsvSanitizer.unescape(input);
        assertThat(unescaped).isEqualTo("The \"Dark\" Knight");
    }

    @Test
    @DisplayName("Should split normal delimited CSV line")
    void shouldSplitNormalCsvLine() {
        String line = "MOV-001|Inception|CAT-01|Christopher Nolan|8.8";
        List<String> tokens = CsvSanitizer.split(line);
        assertThat(tokens).containsExactly("MOV-001", "Inception", "CAT-01", "Christopher Nolan", "8.8");
    }

    @Test
    @DisplayName("Should split CSV line containing quoted delimiters without splitting inside quotes")
    void shouldSplitLineWithQuotedDelimiter() {
        String line = "MOV-002|\"Mission: Impossible | Fallout\"|CAT-02|Christopher McQuarrie|7.7";
        List<String> tokens = CsvSanitizer.split(line);
        assertThat(tokens).containsExactly(
                "MOV-002",
                "Mission: Impossible | Fallout",
                "CAT-02",
                "Christopher McQuarrie",
                "7.7"
        );
    }

    @Test
    @DisplayName("Should correctly join and escape list of tokens")
    void shouldJoinTokensIntoCsvLine() {
        List<String> tokens = Arrays.asList(
                "MOV-003",
                "Fast & Furious | Hobbs & Shaw",
                "CAT-02",
                "Dwayne \"The Rock\" Johnson",
                "6.5"
        );
        String line = CsvSanitizer.join(tokens);
        assertThat(line).isEqualTo(
                "MOV-003|\"Fast & Furious | Hobbs & Shaw\"|CAT-02|\"Dwayne \"\"The Rock\"\" Johnson\"|6.5"
        );

        // Verify round-trip splitting returns identical original tokens
        List<String> parsedTokens = CsvSanitizer.split(line);
        assertThat(parsedTokens).isEqualTo(tokens);
    }
}
