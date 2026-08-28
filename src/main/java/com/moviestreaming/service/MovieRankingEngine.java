package com.moviestreaming.service;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.MovieRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Engine computing automatic multi-weighted movie rankings using logarithmic normalization and
 * extracting Top-K movies via a Max-Heap PriorityQueue.
 */
public class MovieRankingEngine {

    private final MovieRepository movieRepository;
    private final double weightRating;
    private final double weightViews;
    private final double weightFavorites;

    public MovieRankingEngine(MovieRepository movieRepository) {
        this(movieRepository, AppConstants.WEIGHT_RATING, AppConstants.WEIGHT_VIEWS,
                AppConstants.WEIGHT_FAVORITES);
    }

    public MovieRankingEngine(MovieRepository movieRepository, double weightRating,
            double weightViews, double weightFavorites) {
        if (movieRepository == null) {
            throw new IllegalArgumentException("Movie repository must not be null");
        }
        this.movieRepository = movieRepository;
        this.weightRating = weightRating;
        this.weightViews = weightViews;
        this.weightFavorites = weightFavorites;
    }

    /**
     * Data holder representing a Movie paired with its computed ranking score.
     */
    public static class RankedMovie {
        private final Movie movie;
        private final double score;

        public RankedMovie(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }

        public Movie getMovie() {
            return movie;
        }

        public double getScore() {
            return score;
        }

        public double getFormattedScore() {
            return Math.round(score * 100.0) / 100.0;
        }
    }

    /**
     * Calculates the normalized weighted score for a single movie.
     *
     * @param movie movie entity
     * @param maxViews maximum view count across the comparison dataset
     * @param maxFavorites maximum favorites count across the comparison dataset
     * @return normalized score in range [0.0, 1.0]
     */
    public double calculateScore(Movie movie, long maxViews, long maxFavorites) {
        if (movie == null) {
            return 0.0;
        }

        // Rating component: Rating / 10.0 (Rating is bounded 0.0 - 10.0)
        double ratingPart = Math.max(0.0, Math.min(10.0, movie.getRating())) / 10.0;

        // Views component: log10(views + 1) / log10(maxViews + 10)
        double viewDenom = Math.log10(Math.max(0, maxViews) + 10.0);
        double viewPart =
                viewDenom > 0 ? (Math.log10(Math.max(0, movie.getViewCount()) + 1.0) / viewDenom)
                        : 0.0;

        // Favorites component: log10(favorites + 1) / log10(maxFavorites + 10)
        double favDenom = Math.log10(Math.max(0, maxFavorites) + 10.0);
        double favPart =
                favDenom > 0 ? (Math.log10(Math.max(0, movie.getFavoriteCount()) + 1.0) / favDenom)
                        : 0.0;

        return (weightRating * ratingPart) + (weightViews * viewPart) + (weightFavorites * favPart);
    }

    /**
     * Ranks all movies in the repository and returns the Top-K ranked movies.
     *
     * @param limit maximum items to return (if <= 0, returns all)
     * @return list of Top-K RankedMovie items
     */
    public List<RankedMovie> rankMovies(int limit) {
        return rankMovies(movieRepository.findAll(), limit);
    }

    /**
     * Ranks the provided movie list and returns the Top-K ranked movies.
     *
     * @param movies source movie list
     * @param limit maximum items to return (if <= 0, returns all)
     * @return list of Top-K RankedMovie items
     */
    public List<RankedMovie> rankMovies(List<Movie> movies, int limit) {
        if (movies == null || movies.isEmpty()) {
            return Collections.emptyList();
        }

        long maxViews = 0;
        long maxFavorites = 0;
        for (Movie m : movies) {
            if (m != null) {
                if (m.getViewCount() > maxViews) {
                    maxViews = m.getViewCount();
                }
                if (m.getFavoriteCount() > maxFavorites) {
                    maxFavorites = m.getFavoriteCount();
                }
            }
        }

        // Max-Heap PriorityQueue: largest score at the head
        PriorityQueue<RankedMovie> maxHeap =
                new PriorityQueue<>((a, b) -> Double.compare(b.getScore(), a.getScore()));

        for (Movie m : movies) {
            if (m != null) {
                double score = calculateScore(m, maxViews, maxFavorites);
                maxHeap.offer(new RankedMovie(m, score));
            }
        }

        int targetCount = limit > 0 ? Math.min(limit, maxHeap.size()) : maxHeap.size();
        List<RankedMovie> result = new ArrayList<>(targetCount);
        for (int i = 0; i < targetCount; i++) {
            RankedMovie item = maxHeap.poll();
            if (item != null) {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * Returns top ranked Movie entities without the score wrapper.
     *
     * @param limit maximum items to return
     * @return list of Movie entities
     */
    public List<Movie> getTopRankedMovies(int limit) {
        List<RankedMovie> ranked = rankMovies(limit);
        List<Movie> movies = new ArrayList<>(ranked.size());
        for (RankedMovie rm : ranked) {
            movies.add(rm.getMovie());
        }
        return movies;
    }
}
