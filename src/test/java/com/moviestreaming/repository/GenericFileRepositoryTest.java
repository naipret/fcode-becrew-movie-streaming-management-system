package com.moviestreaming.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.util.CsvSanitizer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("GenericFileRepository Test Suite")
class GenericFileRepositoryTest {

    @TempDir
    Path tempDir;

    private Path csvFile;
    private DummyMovieRepository repository;

    static class DummyMovie {
        private final String id;
        private final String title;
        private final double rating;

        DummyMovie(String id, String title, double rating) {
            this.id = id;
            this.title = title;
            this.rating = rating;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public double getRating() {
            return rating;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DummyMovie that = (DummyMovie) o;
            return Double.compare(that.rating, rating) == 0
                    && Objects.equals(id, that.id)
                    && Objects.equals(title, that.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, title, rating);
        }
    }

    static class DummyMovieSerializer implements CsvSerializer<DummyMovie, String> {
        @Override
        public String getHeader() {
            return "id|title|rating";
        }

        @Override
        public String serialize(DummyMovie entity) {
            return CsvSanitizer.join(Arrays.asList(
                    entity.getId(),
                    entity.getTitle(),
                    String.valueOf(entity.getRating())
            ));
        }

        @Override
        public DummyMovie deserialize(String csvLine) {
            List<String> tokens = CsvSanitizer.split(csvLine);
            if (tokens.size() < 3) {
                throw new IllegalArgumentException("Invalid column count: " + tokens.size());
            }
            String id = tokens.get(0);
            String title = tokens.get(1);
            double rating = Double.parseDouble(tokens.get(2));
            return new DummyMovie(id, title, rating);
        }

        @Override
        public String extractId(DummyMovie entity) {
            return entity.getId();
        }
    }

    static class DummyMovieRepository extends GenericFileRepository<DummyMovie, String> {
        DummyMovieRepository(String filePath) {
            super(filePath, new DummyMovieSerializer());
        }
    }

    @BeforeEach
    void setUp() {
        csvFile = tempDir.resolve("movies_test.csv");
        repository = new DummyMovieRepository(csvFile.toString());
    }

    @Test
    @DisplayName("Should save entity to cache and persist to CSV file")
    void shouldSaveAndPersistEntity() throws IOException {
        DummyMovie movie = new DummyMovie("MOV-001", "Inception", 8.8);
        repository.save(movie);

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.existsById("MOV-001")).isTrue();

        Optional<DummyMovie> found = repository.findById("MOV-001");
        assertThat(found).isPresent().contains(movie);

        // Verify CSV file content
        List<String> lines = Files.readAllLines(csvFile);
        assertThat(lines).containsExactly(
                "id|title|rating",
                "MOV-001|Inception|8.8"
        );
    }

    @Test
    @DisplayName("Should delete entity from cache and update CSV file")
    void shouldDeleteEntity() {
        repository.save(new DummyMovie("MOV-001", "Inception", 8.8));
        repository.save(new DummyMovie("MOV-002", "Interstellar", 8.7));

        assertThat(repository.count()).isEqualTo(2);

        repository.deleteById("MOV-001");

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.existsById("MOV-001")).isFalse();
        assertThat(repository.findById("MOV-002")).isPresent();
    }

    @Test
    @DisplayName("Should reload existing data on repository creation")
    void shouldReloadExistingDataOnStartup() {
        repository.save(new DummyMovie("MOV-001", "Inception", 8.8));
        repository.save(new DummyMovie("MOV-002", "Interstellar", 8.7));

        // Create new repository instance pointing to the same file
        DummyMovieRepository reloadedRepo = new DummyMovieRepository(csvFile.toString());
        assertThat(reloadedRepo.count()).isEqualTo(2);
        assertThat(reloadedRepo.findAll()).containsExactly(
                new DummyMovie("MOV-001", "Inception", 8.8),
                new DummyMovie("MOV-002", "Interstellar", 8.7)
        );
    }

    @Test
    @DisplayName("Should resiliently skip corrupted lines during file loading")
    void shouldResilientlyHandleCorruptedCsvLines() throws IOException {
        List<String> mixedLines = Arrays.asList(
                "id|title|rating",
                "MOV-001|Inception|8.8",
                "CORRUPTED_LINE_WITHOUT_COLUMNS",
                "MOV-002|Interstellar|NOT_A_DOUBLE_RATING",
                "MOV-003|The Dark Knight|9.0"
        );
        Files.write(csvFile, mixedLines);

        DummyMovieRepository resilientRepo = new DummyMovieRepository(csvFile.toString());
        // Should successfully load valid rows MOV-001 and MOV-003 while skipping corrupted lines
        assertThat(resilientRepo.count()).isEqualTo(2);
        assertThat(resilientRepo.existsById("MOV-001")).isTrue();
        assertThat(resilientRepo.existsById("MOV-003")).isTrue();
        assertThat(resilientRepo.existsById("MOV-002")).isFalse();
    }
}
