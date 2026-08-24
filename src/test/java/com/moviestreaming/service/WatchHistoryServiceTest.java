package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("WatchHistoryService Playback Tracking Test Suite")
class WatchHistoryServiceTest {

    @TempDir
    Path tempDir;

    private MovieRepository movieRepository;
    private CategoryRepository categoryRepository;
    private MovieService movieService;
    private WatchHistoryRepository historyRepository;
    private WatchHistoryService historyService;
    private Movie movie;

    @BeforeEach
    void setUp() {
        Path catFile = tempDir.resolve("categories.csv");
        Path movieFile = tempDir.resolve("movies.csv");
        Path historyFile = tempDir.resolve("watch_history.csv");

        categoryRepository = new CategoryRepository(catFile.toString());
        movieRepository = new MovieRepository(movieFile.toString());
        categoryRepository.save(new Category("CAT-01", "Sci-Fi", "Science fiction"));

        movieService = new MovieService(movieRepository, categoryRepository);
        movie = movieService.createMovie(new Movie(
                null, "Interstellar", "CAT-01", "Christopher Nolan",
                Collections.singletonList("Matthew McConaughey"), 2014, 100, 8.7, 0, 0, "Space exploration"
        ));

        historyRepository = new WatchHistoryRepository(historyFile.toString());
        historyService = new WatchHistoryService(historyRepository, movieRepository, movieService);
    }

    @Test
    @DisplayName("Should record watch session, increment view count, and set completion status")
    void shouldRecordWatchSession() {
        // Watch 95 minutes of 100 min movie (>= 90% threshold)
        WatchHistoryItem item = historyService.recordWatchSession("USR-001", movie.getId(), 95);

        assertThat(item.getId()).isEqualTo("HIS-001");
        assertThat(item.isCompleted()).isTrue();
        assertThat(item.getWatchedDurationMinutes()).isEqualTo(95);

        // Movie view count should be incremented
        Movie updatedMovie = movieService.getMovieById(movie.getId()).orElse(null);
        assertThat(updatedMovie).isNotNull();
        assertThat(updatedMovie.getViewCount()).isEqualTo(1L);

        // Query history
        List<WatchHistoryItem> historyList = historyService.getHistory("USR-001");
        assertThat(historyList).hasSize(1);
        assertThat(historyList.get(0).getMovieId()).isEqualTo(movie.getId());
    }
}
