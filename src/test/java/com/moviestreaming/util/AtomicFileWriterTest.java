package com.moviestreaming.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("AtomicFileWriter Utility Test Suite")
class AtomicFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should create non-existent parent directories and write file atomically")
    void shouldWriteFileAndCreateDirectories() throws IOException {
        Path targetFile = tempDir.resolve("nested/data/test_movies.csv");
        List<String> lines = Arrays.asList(
                "id|title|rating",
                "MOV-001|Inception|8.8",
                "MOV-002|Interstellar|8.7"
        );

        AtomicFileWriter.writeLines(targetFile.toString(), lines);

        assertThat(Files.exists(targetFile)).isTrue();
        List<String> readLines = Files.readAllLines(targetFile);
        assertThat(readLines).isEqualTo(lines);
    }

    @Test
    @DisplayName("Should overwrite existing file cleanly without leaving temporary files")
    void shouldOverwriteExistingFile() throws IOException {
        Path targetFile = tempDir.resolve("movies.csv");
        List<String> initialLines = Arrays.asList("id|title", "MOV-001|Inception");
        AtomicFileWriter.writeLines(targetFile.toString(), initialLines);

        List<String> updatedLines = Arrays.asList("id|title", "MOV-001|Inception", "MOV-002|Tenet");
        AtomicFileWriter.writeLines(targetFile.toString(), updatedLines);

        assertThat(Files.readAllLines(targetFile)).isEqualTo(updatedLines);

        // Verify temporary file is cleaned up
        Path tempFile = tempDir.resolve("movies.csv.tmp");
        assertThat(Files.exists(tempFile)).isFalse();
    }
}
