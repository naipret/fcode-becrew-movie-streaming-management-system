package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.model.Movie;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SortingService Multi-Attribute Test Suite")
class SortingServiceTest {

    private SortingService sortingService;
    private Movie m1;
    private Movie m2;
    private Movie m3;
    private List<Movie> movieList;

    @BeforeEach
    void setUp() {
        sortingService = new SortingService();

        m1 = new Movie("MOV-001", "Avatar", "CAT-01", "James Cameron",
                Collections.singletonList("Sam Worthington"), 2009, 162, 7.9, 5000L, 1200L, "Synopsis");

        m2 = new Movie("MOV-002", "The Dark Knight", "CAT-02", "Christopher Nolan",
                Collections.singletonList("Christian Bale"), 2008, 152, 9.0, 4500L, 2000L, "Synopsis");

        m3 = new Movie("MOV-003", "Interstellar", "CAT-01", "Christopher Nolan",
                Collections.singletonList("Matthew McConaughey"), 2014, 169, 8.7, 3000L, 1500L, "Synopsis");

        movieList = Arrays.asList(m1, m2, m3);
    }

    @Test
    @DisplayName("Should sort by title A-Z and Z-A")
    void shouldSortByTitle() {
        List<Movie> asc = sortingService.sort(movieList, SortOption.TITLE_ASC);
        assertThat(asc).extracting(Movie::getTitle).containsExactly("Avatar", "Interstellar", "The Dark Knight");

        List<Movie> desc = sortingService.sort(movieList, SortOption.TITLE_DESC);
        assertThat(desc).extracting(Movie::getTitle).containsExactly("The Dark Knight", "Interstellar", "Avatar");
    }

    @Test
    @DisplayName("Should sort by rating highest and lowest first")
    void shouldSortByRating() {
        List<Movie> highest = sortingService.sort(movieList, SortOption.RATING_DESC);
        assertThat(highest).extracting(Movie::getTitle).containsExactly("The Dark Knight", "Interstellar", "Avatar");

        List<Movie> lowest = sortingService.sort(movieList, SortOption.RATING_ASC);
        assertThat(lowest).extracting(Movie::getTitle).containsExactly("Avatar", "Interstellar", "The Dark Knight");
    }

    @Test
    @DisplayName("Should sort by release year newest and oldest first")
    void shouldSortByYear() {
        List<Movie> newest = sortingService.sort(movieList, SortOption.RELEASE_YEAR_DESC);
        assertThat(newest).extracting(Movie::getTitle).containsExactly("Interstellar", "Avatar", "The Dark Knight");

        List<Movie> oldest = sortingService.sort(movieList, SortOption.RELEASE_YEAR_ASC);
        assertThat(oldest).extracting(Movie::getTitle).containsExactly("The Dark Knight", "Avatar", "Interstellar");
    }

    @Test
    @DisplayName("Should sort by popularity (views primary, favorites secondary)")
    void shouldSortByPopularity() {
        List<Movie> popular = sortingService.sort(movieList, SortOption.POPULARITY_DESC);
        assertThat(popular).extracting(Movie::getTitle).containsExactly("Avatar", "The Dark Knight", "Interstellar");
    }
}
