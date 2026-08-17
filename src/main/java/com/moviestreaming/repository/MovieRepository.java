package com.moviestreaming.repository;

import com.moviestreaming.model.Movie;

/**
 * Concrete file-backed repository for Movie entities.
 */
public class MovieRepository extends GenericFileRepository<Movie, String> {

    public MovieRepository(String filePath) {
        super(filePath, new MovieSerializer());
    }
}
