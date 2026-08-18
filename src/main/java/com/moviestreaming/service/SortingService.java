package com.moviestreaming.service;

import com.moviestreaming.model.Movie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service providing multi-attribute sorting for Movie collections.
 * Uses Java 8 Comparator chains for clean, deterministic, and non-destructive ordering.
 */
public class SortingService {

    private final Map<SortOption, Comparator<Movie>> comparatorMap = new EnumMap<>(SortOption.class);

    public SortingService() {
        initComparators();
    }

    private void initComparators() {
        comparatorMap.put(SortOption.TITLE_ASC,
                Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.TITLE_DESC,
                Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());

        comparatorMap.put(SortOption.RATING_DESC,
                Comparator.comparingDouble(Movie::getRating).reversed()
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.RATING_ASC,
                Comparator.comparingDouble(Movie::getRating)
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.RELEASE_YEAR_DESC,
                Comparator.comparingInt(Movie::getReleaseYear).reversed()
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.RELEASE_YEAR_ASC,
                Comparator.comparingInt(Movie::getReleaseYear)
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.POPULARITY_DESC,
                Comparator.comparingLong(Movie::getViewCount)
                        .thenComparingLong(Movie::getFavoriteCount)
                        .thenComparingDouble(Movie::getRating)
                        .reversed());

        comparatorMap.put(SortOption.POPULARITY_ASC,
                Comparator.comparingLong(Movie::getViewCount)
                        .thenComparingLong(Movie::getFavoriteCount)
                        .thenComparingDouble(Movie::getRating));

        comparatorMap.put(SortOption.VIEW_COUNT_DESC,
                Comparator.comparingLong(Movie::getViewCount).reversed()
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.VIEW_COUNT_ASC,
                Comparator.comparingLong(Movie::getViewCount)
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.FAVORITE_COUNT_DESC,
                Comparator.comparingLong(Movie::getFavoriteCount).reversed()
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

        comparatorMap.put(SortOption.FAVORITE_COUNT_ASC,
                Comparator.comparingLong(Movie::getFavoriteCount)
                        .thenComparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Sorts a list of movies using the specified SortOption.
     * Non-destructive: returns a newly created sorted list.
     *
     * @param movies the original list of movies
     * @param option the desired SortOption
     * @return a new list containing movies sorted according to the option
     */
    public List<Movie> sort(List<Movie> movies, SortOption option) {
        if (movies == null || movies.isEmpty()) {
            return Collections.emptyList();
        }
        if (option == null) {
            return new ArrayList<>(movies);
        }

        Comparator<Movie> comparator = comparatorMap.getOrDefault(option, comparatorMap.get(SortOption.TITLE_ASC));
        return movies.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Sorts a list of movies using a custom comparator.
     *
     * @param movies     the original list of movies
     * @param comparator the custom comparator
     * @return a new sorted list
     */
    public List<Movie> sortWith(List<Movie> movies, Comparator<Movie> comparator) {
        if (movies == null || movies.isEmpty()) {
            return Collections.emptyList();
        }
        if (comparator == null) {
            return new ArrayList<>(movies);
        }
        return movies.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
