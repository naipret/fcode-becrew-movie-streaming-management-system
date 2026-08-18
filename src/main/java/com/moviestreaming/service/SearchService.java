package com.moviestreaming.service;

import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.MovieRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * High-level Search Engine service utilizing the in-memory inverted IndexingService.
 * Coordinates multi-field queries and sorting pipelines.
 */
public class SearchService {

    private final IndexingService indexingService;
    private final MovieRepository movieRepository;
    private final SortingService sortingService;

    public SearchService(IndexingService indexingService, MovieRepository movieRepository, SortingService sortingService) {
        if (indexingService == null || movieRepository == null || sortingService == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.indexingService = indexingService;
        this.movieRepository = movieRepository;
        this.sortingService = sortingService;
    }

    /**
     * Searches movies by title keywords.
     *
     * @param query title search query
     * @return list of matching Movie objects
     */
    public List<Movie> searchByTitle(String query) {
        Set<String> movieIds = indexingService.searchMovieIdsByTitle(query);
        return resolveMovies(movieIds);
    }

    /**
     * Searches movies by actor name or token.
     *
     * @param query actor search query
     * @return list of matching Movie objects
     */
    public List<Movie> searchByActor(String query) {
        Set<String> movieIds = indexingService.searchMovieIdsByActor(query);
        return resolveMovies(movieIds);
    }

    /**
     * Searches movies by director name or token.
     *
     * @param query director search query
     * @return list of matching Movie objects
     */
    public List<Movie> searchByDirector(String query) {
        Set<String> movieIds = indexingService.searchMovieIdsByDirector(query);
        return resolveMovies(movieIds);
    }

    /**
     * Searches movies by genre name or category ID.
     *
     * @param query genre or category query
     * @return list of matching Movie objects
     */
    public List<Movie> searchByGenre(String query) {
        Set<String> movieIds = indexingService.searchMovieIdsByGenre(query);
        return resolveMovies(movieIds);
    }

    /**
     * Searches movies universally across all fields (Title, Actors, Director, Genre, Synopsis).
     *
     * @param keyword the search keyword
     * @return list of matching Movie objects
     */
    public List<Movie> searchUniversal(String keyword) {
        Set<String> movieIds = indexingService.searchMovieIdsUniversal(keyword);
        return resolveMovies(movieIds);
    }

    /**
     * Searches movies universally and sorts the result with a specified SortOption.
     *
     * @param keyword    the search keyword
     * @param sortOption the desired sorting order
     * @return list of sorted matching movies
     */
    public List<Movie> searchUniversalAndSort(String keyword, SortOption sortOption) {
        List<Movie> results = searchUniversal(keyword);
        return sortingService.sort(results, sortOption);
    }

    private List<Movie> resolveMovies(Set<String> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return Collections.emptyList();
        }

        return movieIds.stream()
                .map(id -> movieRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
