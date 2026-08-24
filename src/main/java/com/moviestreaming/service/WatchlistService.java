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
 * Service managing user watchlists with file persistence.
 */
public class WatchlistService {

    private final UserListRepository watchlistRepository;
    private final MovieRepository movieRepository;

    public WatchlistService(UserListRepository watchlistRepository, MovieRepository movieRepository) {
        if (watchlistRepository == null || movieRepository == null) {
            throw new IllegalArgumentException("Repositories must not be null");
        }
        this.watchlistRepository = watchlistRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * Adds a movie to the user's watchlist.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     */
    public void addToWatchlist(String userId, String movieId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("User ID cannot be empty.");
        }
        if (movieId == null || movieId.trim().isEmpty()) {
            throw new ValidationException("Movie ID cannot be empty.");
        }

        if (!movieRepository.existsById(movieId.trim())) {
            throw new EntityNotFoundException("Movie", movieId);
        }

        boolean added = watchlistRepository.add(userId.trim(), movieId.trim());
        if (!added) {
            throw new ValidationException("Movie is already in your watchlist.");
        }
    }

    /**
     * Removes a movie from the user's watchlist.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     */
    public void removeFromWatchlist(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return;
        }
        watchlistRepository.remove(userId.trim(), movieId.trim());
    }

    /**
     * Retrieves all movies currently in the user's watchlist.
     *
     * @param userId the user ID
     * @return list of Movie entities
     */
    public List<Movie> getWatchlist(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> movieIds = watchlistRepository.getMovieIds(userId.trim());
        return movieIds.stream()
                .map(id -> movieRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a movie is in the user's watchlist.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     * @return true if present in watchlist
     */
    public boolean isInWatchlist(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return false;
        }
        return watchlistRepository.contains(userId.trim(), movieId.trim());
    }
}
