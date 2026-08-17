package com.moviestreaming.validator;

import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Movie;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain validator for Movie entities.
 */
public class MovieValidator implements Validator<Movie> {

    private static final int EARLIEST_MOVIE_YEAR = 1888;
    private static final int MAX_DURATION_MINUTES = 1000;

    @Override
    public void validate(Movie movie) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (movie == null) {
            throw new ValidationException("Movie cannot be null");
        }

        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            errors.add("Movie title cannot be empty");
        }

        if (movie.getCategoryId() == null || movie.getCategoryId().trim().isEmpty()) {
            errors.add("Category ID cannot be empty");
        }

        if (movie.getDirector() == null || movie.getDirector().trim().isEmpty()) {
            errors.add("Director name cannot be empty");
        }

        if (movie.getActors() == null || movie.getActors().isEmpty()) {
            errors.add("At least one actor must be specified");
        }

        int maxAllowedYear = Year.now().getValue() + 5;
        if (movie.getReleaseYear() < EARLIEST_MOVIE_YEAR || movie.getReleaseYear() > maxAllowedYear) {
            errors.add(String.format("Release year must be between %d and %d", EARLIEST_MOVIE_YEAR, maxAllowedYear));
        }

        if (movie.getDurationMinutes() <= 0 || movie.getDurationMinutes() > MAX_DURATION_MINUTES) {
            errors.add(String.format("Duration must be between 1 and %d minutes", MAX_DURATION_MINUTES));
        }

        if (movie.getRating() < 0.0 || movie.getRating() > 10.0) {
            errors.add("Rating must be between 0.0 and 10.0");
        }

        if (movie.getViewCount() < 0) {
            errors.add("View count cannot be negative");
        }

        if (movie.getFavoriteCount() < 0) {
            errors.add("Favorite count cannot be negative");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
