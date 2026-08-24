package com.moviestreaming.service;

import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserListRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing user favorite movies with file persistence and real-time movie counter syncing.
 */
public class FavoriteService {

    private final UserListRepository favoriteRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    public FavoriteService(UserListRepository favoriteRepository, MovieRepository movieRepository,
                           MovieService movieService) {
        if (favoriteRepository == null || movieRepository == null || movieService == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.favoriteRepository = favoriteRepository;
        this.movieRepository = movieRepository;
        this.movieService = movieService;
    }

    /**
     * Adds a movie to the user's favorites and increments the movie favorite count.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     */
    public void addToFavorites(String userId, String movieId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("User ID cannot be empty.");
        }
        if (movieId == null || movieId.trim().isEmpty()) {
            throw new ValidationException("Movie ID cannot be empty.");
        }

        if (!movieRepository.existsById(movieId.trim())) {
            throw new EntityNotFoundException("Movie", movieId);
        }

        boolean added = favoriteRepository.add(userId.trim(), movieId.trim());
        if (!added) {
            throw new ValidationException("Movie is already in your favorites.");
        }

        // Increment movie favorite counter
        movieService.incrementFavoriteCount(movieId.trim());
    }

    /**
     * Removes a movie from the user's favorites and decrements the movie favorite count.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     */
    public void removeFromFavorites(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return;
        }
        boolean removed = favoriteRepository.remove(userId.trim(), movieId.trim());
        if (removed) {
            // Decrement movie favorite counter
            movieService.decrementFavoriteCount(movieId.trim());
        }
    }

    /**
     * Retrieves all favorite movies for a user.
     *
     * @param userId the user ID
     * @return list of favorite Movie entities
     */
    public List<Movie> getFavorites(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> movieIds = favoriteRepository.getMovieIds(userId.trim());
        return movieIds.stream()
                .map(id -> movieRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a movie is in the user's favorites.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     * @return true if marked favorite
     */
    public boolean isFavorite(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return false;
        }
        return favoriteRepository.contains(userId.trim(), movieId.trim());
    }
}
