package com.moviestreaming.service;

import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.validator.MovieValidator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing Movie CRUD operations, business rules, category foreign key checks,
 * and view/favorite counters.
 */
public class MovieService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final MovieValidator validator;

    public MovieService(MovieRepository movieRepository, CategoryRepository categoryRepository) {
        if (movieRepository == null || categoryRepository == null) {
            throw new IllegalArgumentException("Repositories must not be null");
        }
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.validator = new MovieValidator();
    }

    /**
     * Creates and persists a new Movie with validation and auto-assigned ID if missing.
     *
     * @param movie the Movie entity to create
     * @return the created Movie
     */
    public Movie createMovie(Movie movie) {
        if (movie == null) {
            throw new ValidationException("Movie cannot be null");
        }

        // Validate basic fields
        validator.validate(movie);

        // Check category exists (Foreign Key check)
        if (!categoryRepository.existsById(movie.getCategoryId())) {
            throw new ValidationException(String.format("Category with ID '%s' does not exist.", movie.getCategoryId()));
        }

        // Check uniqueness of Title + ReleaseYear
        checkTitleAndYearUniqueness(movie.getTitle(), movie.getReleaseYear(), null);

        // Auto-assign ID if not provided
        if (movie.getId() == null || movie.getId().trim().isEmpty()) {
            movie.setId(generateNextId());
        }

        return movieRepository.save(movie);
    }

    /**
     * Updates an existing Movie entity.
     *
     * @param movie the updated movie entity
     * @return the saved Movie
     */
    public Movie updateMovie(Movie movie) {
        if (movie == null || movie.getId() == null) {
            throw new ValidationException("Movie and Movie ID cannot be null for update");
        }

        Movie existing = movieRepository.findById(movie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Movie", movie.getId()));

        validator.validate(movie);

        if (!categoryRepository.existsById(movie.getCategoryId())) {
            throw new ValidationException(String.format("Category with ID '%s' does not exist.", movie.getCategoryId()));
        }

        checkTitleAndYearUniqueness(movie.getTitle(), movie.getReleaseYear(), movie.getId());

        return movieRepository.save(movie);
    }

    /**
     * Deletes a Movie by its ID.
     *
     * @param id the movie ID
     */
    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            throw new EntityNotFoundException("Movie", id);
        }
        movieRepository.deleteById(id);
    }

    /**
     * Finds a Movie by ID.
     *
     * @param id the movie ID
     * @return Optional containing Movie if found
     */
    public Optional<Movie> getMovieById(String id) {
        return movieRepository.findById(id);
    }

    /**
     * Retrieves all movies.
     *
     * @return list of all movies
     */
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    /**
     * Retrieves all movies belonging to a specific Category ID.
     *
     * @param categoryId the category ID
     * @return list of matching movies
     */
    public List<Movie> getMoviesByCategory(String categoryId) {
        if (categoryId == null) {
            return java.util.Collections.emptyList();
        }
        return movieRepository.findAll().stream()
                .filter(m -> m.getCategoryId() != null && m.getCategoryId().equalsIgnoreCase(categoryId.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Increments the view count of a movie by 1.
     *
     * @param movieId the movie ID
     */
    public void incrementViewCount(String movieId) {
        movieRepository.findById(movieId).ifPresent(movie -> {
            movie.setViewCount(movie.getViewCount() + 1);
            movieRepository.save(movie);
        });
    }

    /**
     * Increments the favorite count of a movie by 1.
     *
     * @param movieId the movie ID
     */
    public void incrementFavoriteCount(String movieId) {
        movieRepository.findById(movieId).ifPresent(movie -> {
            movie.setFavoriteCount(movie.getFavoriteCount() + 1);
            movieRepository.save(movie);
        });
    }

    /**
     * Decrements the favorite count of a movie by 1.
     *
     * @param movieId the movie ID
     */
    public void decrementFavoriteCount(String movieId) {
        movieRepository.findById(movieId).ifPresent(movie -> {
            movie.setFavoriteCount(Math.max(0, movie.getFavoriteCount() - 1));
            movieRepository.save(movie);
        });
    }

    private void checkTitleAndYearUniqueness(String title, int releaseYear, String excludeId) {
        boolean duplicate = movieRepository.findAll().stream()
                .filter(m -> excludeId == null || !m.getId().equals(excludeId))
                .anyMatch(m -> m.getTitle() != null
                        && m.getTitle().equalsIgnoreCase(title.trim())
                        && m.getReleaseYear() == releaseYear);

        if (duplicate) {
            throw new DuplicateEntityException("Movie", "title and release year", title + " (" + releaseYear + ")");
        }
    }

    private synchronized String generateNextId() {
        int maxIndex = movieRepository.findAll().stream()
                .map(Movie::getId)
                .filter(id -> id != null && id.startsWith("MOV-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(4));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("MOV-%03d", maxIndex + 1);
    }
}
