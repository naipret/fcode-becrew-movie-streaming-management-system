package com.moviestreaming.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain model representing a many-to-many link between a User and a Movie (e.g., Watchlist or Favorites).
 */
public class UserMoviePair implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String movieId;

    public UserMoviePair() {
    }

    public UserMoviePair(String userId, String movieId) {
        this.userId = userId;
        this.movieId = movieId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserMoviePair that = (UserMoviePair) o;
        return Objects.equals(userId, that.userId) && Objects.equals(movieId, that.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, movieId);
    }

    @Override
    public String toString() {
        return "UserMoviePair{"
                + "userId='" + userId + '\''
                + ", movieId='" + movieId + '\''
                + '}';
    }
}
