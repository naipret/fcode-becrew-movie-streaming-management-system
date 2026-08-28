package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.MovieRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Movie Ranking Engine (Weighted Max-Heap) Test Suite")
class MovieRankingEngineTest {

    private Path tempDir;
    private MovieRankingEngine rankingEngine;
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("ranking_test");
        String moviesPath = tempDir.resolve("movies.csv").toString();

        List<String> movieLines = Arrays.asList(
                "id|title|categoryId|director|actors|releaseYear|durationMinutes|rating|viewCount|favoriteCount|synopsis",
                "MOV-001|Low Rating High Views|CAT-01|Director A|Actor A|2020|120|5.0|100000|50000|Popular movie.",
                "MOV-002|High Rating Low Views|CAT-01|Director B|Actor B|2021|130|9.8|50|20|Critically acclaimed movie.",
                "MOV-003|Balanced High Movie|CAT-02|Director C|Actor C|2022|140|9.0|80000|40000|Blockbuster hit.",
                "MOV-004|Zero Stats Movie|CAT-03|Director D|Actor D|2023|110|0.0|0|0|Brand new movie.");
        Files.write(tempDir.resolve("movies.csv"), movieLines);
        movieRepository = new MovieRepository(moviesPath);
        rankingEngine = new MovieRankingEngine(movieRepository);
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
    @DisplayName("Should correctly calculate normalized weighted score")
    void shouldCalculateNormalizedScore() {
        Movie movie = movieRepository.findById("MOV-003").orElse(null);
        assertThat(movie).isNotNull();

        double score = rankingEngine.calculateScore(movie, 100000, 50000);
        assertThat(score).isGreaterThan(0.0).isLessThanOrEqualTo(1.0);
    }

    @Test
    @DisplayName("Should safely handle movies with zero views, zero favorites, and zero rating")
    void shouldHandleZeroStatsSafely() {
        Movie movie = movieRepository.findById("MOV-004").orElse(null);
        assertThat(movie).isNotNull();

        double score = rankingEngine.calculateScore(movie, 0, 0);
        assertThat(score).isEqualTo(0.0);
        assertThat(Double.isNaN(score)).isFalse();
        assertThat(Double.isInfinite(score)).isFalse();
    }

    @Test
    @DisplayName("Should rank movies in descending order via Max-Heap")
    void shouldRankMoviesDescending() {
        List<MovieRankingEngine.RankedMovie> ranked = rankingEngine.rankMovies(10);
        assertThat(ranked).hasSize(4);

        // Scores should be strictly descending
        for (int i = 0; i < ranked.size() - 1; i++) {
            assertThat(ranked.get(i).getScore())
                    .isGreaterThanOrEqualTo(ranked.get(i + 1).getScore());
        }

        // The top ranked should be the balanced high movie (MOV-003)
        assertThat(ranked.get(0).getMovie().getId()).isEqualTo("MOV-003");
        // The lowest ranked should be zero stats movie (MOV-004)
        assertThat(ranked.get(ranked.size() - 1).getMovie().getId()).isEqualTo("MOV-004");
    }

    @Test
    @DisplayName("Should respect Top-K limit parameter")
    void shouldRespectTopKLimit() {
        List<MovieRankingEngine.RankedMovie> top2 = rankingEngine.rankMovies(2);
        assertThat(top2).hasSize(2);

        List<Movie> top2Movies = rankingEngine.getTopRankedMovies(2);
        assertThat(top2Movies).hasSize(2);
        assertThat(top2Movies.get(0).getId()).isEqualTo(top2.get(0).getMovie().getId());
    }

    @Test
    @DisplayName("Should return empty list when ranking empty dataset")
    void shouldHandleEmptyDataset() {
        List<MovieRankingEngine.RankedMovie> empty =
                rankingEngine.rankMovies(Collections.emptyList(), 5);
        assertThat(empty).isEmpty();
    }
}
