package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("MovieService Test Suite")
class MovieServiceTest {

    @TempDir
    Path tempDir;

    private CategoryRepository categoryRepository;
    private MovieRepository movieRepository;
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        Path catFile = tempDir.resolve("categories.csv");
        Path movieFile = tempDir.resolve("movies.csv");

        categoryRepository = new CategoryRepository(catFile.toString());
        movieRepository = new MovieRepository(movieFile.toString());
        movieService = new MovieService(movieRepository, categoryRepository);

        // Seed default category
        categoryRepository.save(new Category("CAT-01", "Sci-Fi", "Science fiction"));
        categoryRepository.save(new Category("CAT-02", "Action", "Action movies"));
    }

    @Test
    @DisplayName("Should create movie with auto-generated ID when category exists")
    void shouldCreateMovie() {
        Movie movie = new Movie(
                null, "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 8.8, 0, 0, "Dream espionage"
        );

        Movie created = movieService.createMovie(movie);
        assertThat(created.getId()).isEqualTo("MOV-001");
        assertThat(created.getTitle()).isEqualTo("Inception");
        assertThat(movieService.getAllMovies()).hasSize(1);
    }

    @Test
    @DisplayName("Should reject movie creation if category does not exist")
    void shouldRejectMovieWithInvalidCategory() {
        Movie movie = new Movie(
                null, "Inception", "CAT-999", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 8.8, 0, 0, "Dream espionage"
        );

        assertThatThrownBy(() -> movieService.createMovie(movie))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category with ID 'CAT-999' does not exist");
    }

    @Test
    @DisplayName("Should reject movie creation if title and release year already exist")
    void shouldRejectDuplicateMovie() {
        Movie movie1 = new Movie(
                null, "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 8.8, 0, 0, "Original"
        );
        movieService.createMovie(movie1);

        Movie movie2 = new Movie(
                null, "inception", "CAT-02", "Another Director",
                Arrays.asList("Actor"), 2010, 120, 7.0, 0, 0, "Duplicate"
        );

        assertThatThrownBy(() -> movieService.createMovie(movie2))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("inception (2010)");
    }

    @Test
    @DisplayName("Should update movie and handle category filtering & counters")
    void shouldUpdateMovieAndFilterByCategory() {
        Movie movie1 = movieService.createMovie(new Movie(
                null, "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 8.8, 0, 0, "Original"
        ));
        movieService.createMovie(new Movie(
                null, "The Dark Knight", "CAT-02", "Christopher Nolan",
                Arrays.asList("Christian Bale"), 2008, 152, 9.0, 0, 0, "Batman"
        ));

        List<Movie> sciFiMovies = movieService.getMoviesByCategory("CAT-01");
        assertThat(sciFiMovies).hasSize(1).extracting(Movie::getTitle).containsExactly("Inception");

        // Test counters
        movieService.incrementViewCount(movie1.getId());
        movieService.incrementFavoriteCount(movie1.getId());

        Optional<Movie> updated = movieService.getMovieById(movie1.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getViewCount()).isEqualTo(1L);
        assertThat(updated.get().getFavoriteCount()).isEqualTo(1L);

        movieService.decrementFavoriteCount(movie1.getId());
        assertThat(movieService.getMovieById(movie1.getId()).get().getFavoriteCount()).isEqualTo(0L);

        // Delete movie
        movieService.deleteMovie(movie1.getId());
        assertThat(movieService.getMovieById(movie1.getId())).isEmpty();
    }
}
