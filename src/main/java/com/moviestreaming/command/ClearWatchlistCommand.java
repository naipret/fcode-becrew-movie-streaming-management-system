package com.moviestreaming.command;

import com.moviestreaming.model.Movie;
import com.moviestreaming.service.WatchlistService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command to clear all movies from a user's watchlist with full snapshot recovery.
 */
public class ClearWatchlistCommand implements WatchlistCommand {

    private final WatchlistService watchlistService;
    private final String userId;
    private final List<String> snapshotMovieIds;

    public ClearWatchlistCommand(WatchlistService watchlistService, String userId) {
        if (watchlistService == null || userId == null) {
            throw new IllegalArgumentException("Arguments must not be null");
        }
        this.watchlistService = watchlistService;
        this.userId = userId;
        List<Movie> currentList = watchlistService.getWatchlist(userId);
        List<String> ids = new ArrayList<>();
        for (Movie m : currentList) {
            ids.add(m.getId());
        }
        this.snapshotMovieIds = Collections.unmodifiableList(ids);
    }

    @Override
    public void execute() {
        for (String movieId : snapshotMovieIds) {
            watchlistService.removeFromWatchlist(userId, movieId);
        }
    }

    @Override
    public void undo() {
        for (String movieId : snapshotMovieIds) {
            watchlistService.addToWatchlist(userId, movieId);
        }
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getDescription() {
        return "Clear Watchlist (" + snapshotMovieIds.size() + " items removed)";
    }
}
