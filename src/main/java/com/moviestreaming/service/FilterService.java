package com.moviestreaming.service;

import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.specification.MovieFilterCriteria;
import com.moviestreaming.specification.MovieSpecifications;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service executing multi-condition filtering on Movie catalogs using the Specification Pattern.
 */
public class FilterService {

    private final MovieRepository movieRepository;

    public FilterService(MovieRepository movieRepository) {
        if (movieRepository == null) {
            throw new IllegalArgumentException("Movie repository must not be null");
        }
        this.movieRepository = movieRepository;
    }

    /**
     * Filters all movies in the repository matching the provided criteria.
     *
     * @param criteria filter criteria container
     * @return filtered list of movies
     */
    public List<Movie> filter(MovieFilterCriteria criteria) {
        return filter(movieRepository.findAll(), criteria);
    }

    /**
     * Filters a given source list of movies matching the provided criteria.
     *
     * @param sourceList source movie list
     * @param criteria filter criteria container
     * @return filtered list of movies
     */
    public List<Movie> filter(List<Movie> sourceList, MovieFilterCriteria criteria) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        if (criteria == null || criteria.isEmpty()) {
            return sourceList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }

        Predicate<Movie> specification = MovieSpecifications.fromCriteria(criteria);
        return sourceList.stream().filter(specification).collect(Collectors.toList());
    }
}
