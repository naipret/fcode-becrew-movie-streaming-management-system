package com.moviestreaming.command;

import com.moviestreaming.service.WatchlistService;

/**
 * Command to add a movie to a user's watchlist with undo/redo capability.
 */
public class AddToWatchlistCommand implements WatchlistCommand {

    private final WatchlistService watchlistService;
    private final String userId;
    private final String movieId;
    private final String movieTitle;

    public AddToWatchlistCommand(WatchlistService watchlistService, String userId, String movieId,
            String movieTitle) {
        if (watchlistService == null || userId == null || movieId == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        this.watchlistService = watchlistService;
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle != null ? movieTitle : movieId;
    }

    @Override
    public void execute() {
        watchlistService.addToWatchlist(userId, movieId);
    }

    @Override
    public void undo() {
        watchlistService.removeFromWatchlist(userId, movieId);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getDescription() {
        return "Add '" + movieTitle + "' (" + movieId + ") to Watchlist";
    }
}
