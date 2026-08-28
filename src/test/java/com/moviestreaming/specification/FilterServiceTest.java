package com.moviestreaming.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.service.FilterService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Specification Pattern & FilterService Test Suite")
class FilterServiceTest {

    private Path tempDir;
    private FilterService filterService;
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("filter_test");
        String moviesPath = tempDir.resolve("movies.csv").toString();

        List<String> movieLines = Arrays.asList(
                "id|title|categoryId|director|actors|releaseYear|durationMinutes|rating|viewCount|favoriteCount|synopsis",
                "MOV-001|Inception|CAT-01|Christopher Nolan|Leonardo DiCaprio,Joseph Gordon-Levitt|2010|148|8.8|12500|3200|Dream heist.",
                "MOV-002|Interstellar|CAT-01|Christopher Nolan|Matthew McConaughey,Anne Hathaway|2014|169|8.7|18900|4500|Space journey.",
                "MOV-003|The Dark Knight|CAT-02|Christopher Nolan|Christian Bale,Heath Ledger|2008|152|9.0|25000|8000|Gotham protector.",
                "MOV-004|Spirited Away|CAT-03|Hayao Miyazaki|Rumi Hiiragi,Miyu Irino|2001|125|8.6|9500|2700|Fantasy spirit world.",
                "MOV-005|Avengers: Endgame|CAT-02|Anthony Russo|Robert Downey Jr.,Chris Evans|2019|181|8.4|40000|12000|Superhero finale.");
        Files.write(tempDir.resolve("movies.csv"), movieLines);
        movieRepository = new MovieRepository(moviesPath);
        filterService = new FilterService(movieRepository);
    }

    @AfterEach
    void tearDown() {
        if (tempDir != null) {
            File[] files = tempDir.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            tempDir.toFile().delete();
        }
    }

    @Test
    @DisplayName("Should return all movies when criteria is empty")
    void shouldReturnAllMoviesWhenCriteriaEmpty() {
        MovieFilterCriteria criteria = new MovieFilterCriteria();
        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(5);
    }

    @Test
    @DisplayName("Should filter by single category specification")
    void shouldFilterByCategory() {
        MovieFilterCriteria criteria = new MovieFilterCriteria().setCategoryId("CAT-01");
        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(2).extracting(Movie::getId).containsExactlyInAnyOrder("MOV-001",
                "MOV-002");
    }

    @Test
    @DisplayName("Should filter by minimum rating specification")
    void shouldFilterByMinRating() {
        MovieFilterCriteria criteria = new MovieFilterCriteria().setMinRating(8.8);
        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(2).extracting(Movie::getId).containsExactlyInAnyOrder("MOV-001",
                "MOV-003");
    }

    @Test
    @DisplayName("Should filter by release year range")
    void shouldFilterByYearRange() {
        MovieFilterCriteria criteria = new MovieFilterCriteria().setFromYear(2005).setToYear(2012);
        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(2).extracting(Movie::getId).containsExactlyInAnyOrder("MOV-001",
                "MOV-003");
    }

    @Test
    @DisplayName("Should filter by actor name case-insensitively")
    void shouldFilterByActor() {
        MovieFilterCriteria criteria = new MovieFilterCriteria().setActorName("dicaprio");
        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(1).extracting(Movie::getTitle).containsExactly("Inception");
    }

    @Test
    @DisplayName("Should filter by director name")
    void shouldFilterByDirector() {
        MovieFilterCriteria criteria = new MovieFilterCriteria().setDirectorName("Nolan");
        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Should filter combining multiple criteria (Category AND Rating AND Actor)")
    void shouldFilterCombinedCriteria() {
        MovieFilterCriteria criteria = new MovieFilterCriteria().setCategoryId("CAT-01")
                .setMinRating(8.7).setActorName("Matthew");

        List<Movie> results = filterService.filter(criteria);
        assertThat(results).hasSize(1).extracting(Movie::getTitle).containsExactly("Interstellar");
    }

    @Test
    @DisplayName("Should return empty list when no movies match criteria")
    void shouldReturnEmptyWhenNoMatch() {
        MovieFilterCriteria criteria =
                new MovieFilterCriteria().setCategoryId("CAT-99").setMinRating(9.9);

        List<Movie> results = filterService.filter(criteria);
        assertThat(results).isEmpty();
    }
}
