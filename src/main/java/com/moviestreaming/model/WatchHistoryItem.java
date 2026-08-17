package com.moviestreaming.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a playback log or viewing session.
 */
public class WatchHistoryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String movieId;
    private int watchedDurationMinutes;
    private int totalDurationMinutes;
    private LocalDateTime lastWatchedTimestamp;
    private boolean completed;

    public WatchHistoryItem() {
    }

    public WatchHistoryItem(String id, String userId, String movieId, int watchedDurationMinutes,
                            int totalDurationMinutes, LocalDateTime lastWatchedTimestamp, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.watchedDurationMinutes = watchedDurationMinutes;
        this.totalDurationMinutes = totalDurationMinutes;
        this.lastWatchedTimestamp = lastWatchedTimestamp;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getWatchedDurationMinutes() {
        return watchedDurationMinutes;
    }

    public void setWatchedDurationMinutes(int watchedDurationMinutes) {
        this.watchedDurationMinutes = watchedDurationMinutes;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public LocalDateTime getLastWatchedTimestamp() {
        return lastWatchedTimestamp;
    }

    public void setLastWatchedTimestamp(LocalDateTime lastWatchedTimestamp) {
        this.lastWatchedTimestamp = lastWatchedTimestamp;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WatchHistoryItem that = (WatchHistoryItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WatchHistoryItem{"
                + "id='" + id + '\''
                + ", userId='" + userId + '\''
                + ", movieId='" + movieId + '\''
                + ", watchedDurationMinutes=" + watchedDurationMinutes
                + ", totalDurationMinutes=" + totalDurationMinutes
                + ", completed=" + completed
                + '}';
    }
}
