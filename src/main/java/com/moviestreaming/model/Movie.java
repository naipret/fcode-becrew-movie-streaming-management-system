package com.moviestreaming.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a movie entry in the streaming platform.
 */
public class Movie implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String categoryId;
    private String director;
    private List<String> actors = new ArrayList<>();
    private int releaseYear;
    private int durationMinutes;
    private double rating;
    private long viewCount;
    private long favoriteCount;
    private String synopsis;

    public Movie() {
    }

    public Movie(String id, String title, String categoryId, String director,
                 List<String> actors, int releaseYear, int durationMinutes,
                 double rating, long viewCount, long favoriteCount, String synopsis) {
        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.director = director;
        if (actors != null) {
            this.actors = new ArrayList<>(actors);
        }
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.viewCount = viewCount;
        this.favoriteCount = favoriteCount;
        this.synopsis = synopsis;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public List<String> getActors() {
        return Collections.unmodifiableList(actors);
    }

    public void setActors(List<String> actors) {
        this.actors = (actors != null) ? new ArrayList<>(actors) : new ArrayList<>();
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Movie{"
                + "id='" + id + '\''
                + ", title='" + title + '\''
                + ", categoryId='" + categoryId + '\''
                + ", director='" + director + '\''
                + ", releaseYear=" + releaseYear
                + ", durationMinutes=" + durationMinutes
                + ", rating=" + rating
                + ", viewCount=" + viewCount
                + ", favoriteCount=" + favoriteCount
                + '}';
    }
}
