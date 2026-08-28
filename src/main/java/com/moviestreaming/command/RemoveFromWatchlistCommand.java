package com.moviestreaming.command;

import com.moviestreaming.service.WatchlistService;

/**
 * Command to remove a movie from a user's watchlist with undo/redo capability.
 */
public class RemoveFromWatchlistCommand implements WatchlistCommand {

    private final WatchlistService watchlistService;
    private final String userId;
    private final String movieId;
    private final String movieTitle;

    public RemoveFromWatchlistCommand(WatchlistService watchlistService, String userId,
            String movieId, String movieTitle) {
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
        watchlistService.removeFromWatchlist(userId, movieId);
    }

    @Override
    public void undo() {
        watchlistService.addToWatchlist(userId, movieId);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getDescription() {
        return "Remove '" + movieTitle + "' (" + movieId + ") from Watchlist";
    }
}
