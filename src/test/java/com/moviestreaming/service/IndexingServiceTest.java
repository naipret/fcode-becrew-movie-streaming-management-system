package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IndexingService Inverted Index Test Suite")
class IndexingServiceTest {

    private IndexingService indexingService;
    private Movie inception;
    private Movie interstellar;
    private Category sciFi;

    @BeforeEach
    void setUp() {
        indexingService = new IndexingService();

        sciFi = new Category("CAT-01", "Sci-Fi", "Science Fiction");

        inception = new Movie(
                "MOV-001", "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page"),
                2010, 148, 8.8, 1000L, 500L, "A thief who steals corporate secrets through dream-sharing technology."
        );

        interstellar = new Movie(
                "MOV-002", "Interstellar", "CAT-01", "Christopher Nolan",
                Arrays.asList("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
                2014, 169, 8.7, 950L, 480L, "A team of explorers travel through a wormhole in space."
        );

        indexingService.initialize(Arrays.asList(inception, interstellar), Collections.singletonList(sciFi));
    }

    @Test
    @DisplayName("Should find movies by exact and partial title tokens")
    void shouldFindMoviesByTitleTokens() {
        Set<String> ids = indexingService.searchMovieIdsByTitle("Inception");
        assertThat(ids).containsExactly("MOV-001");

        Set<String> stellarIds = indexingService.searchMovieIdsByTitle("interstellar");
        assertThat(stellarIds).containsExactly("MOV-002");
    }

    @Test
    @DisplayName("Should find movies by actor full name and individual actor tokens")
    void shouldFindMoviesByActor() {
        Set<String> diCaprioMovies = indexingService.searchMovieIdsByActor("Leonardo DiCaprio");
        assertThat(diCaprioMovies).containsExactly("MOV-001");

        Set<String> mcConaugheyMovies = indexingService.searchMovieIdsByActor("Matthew");
        assertThat(mcConaugheyMovies).containsExactly("MOV-002");
    }

    @Test
    @DisplayName("Should find all movies by director across different titles")
    void shouldFindMoviesByDirector() {
        Set<String> nolanMovies = indexingService.searchMovieIdsByDirector("Christopher Nolan");
        assertThat(nolanMovies).containsExactlyInAnyOrder("MOV-001", "MOV-002");

        Set<String> nolanToken = indexingService.searchMovieIdsByDirector("Nolan");
        assertThat(nolanToken).containsExactlyInAnyOrder("MOV-001", "MOV-002");
    }

    @Test
    @DisplayName("Should find movies by category ID and genre name")
    void shouldFindMoviesByGenre() {
        Set<String> byCatId = indexingService.searchMovieIdsByGenre("CAT-01");
        assertThat(byCatId).containsExactlyInAnyOrder("MOV-001", "MOV-002");

        Set<String> byGenreName = indexingService.searchMovieIdsByGenre("Sci-Fi");
        assertThat(byGenreName).containsExactlyInAnyOrder("MOV-001", "MOV-002");
    }

    @Test
    @DisplayName("Should support universal search across title, synopsis, and crew")
    void shouldSearchUniversally() {
        Set<String> dreamMatches = indexingService.searchMovieIdsUniversal("dream");
        assertThat(dreamMatches).containsExactly("MOV-001");

        Set<String> wormholeMatches = indexingService.searchMovieIdsUniversal("wormhole");
        assertThat(wormholeMatches).containsExactly("MOV-002");
    }

    @Test
    @DisplayName("Should handle incremental indexing and eviction properly")
    void shouldHandleIncrementalIndexAndEviction() {
        indexingService.evictMovie("MOV-001");
        assertThat(indexingService.searchMovieIdsByTitle("Inception")).isEmpty();

        // Re-index
        indexingService.indexMovie(inception, sciFi);
        assertThat(indexingService.searchMovieIdsByTitle("Inception")).containsExactly("MOV-001");
    }
}
