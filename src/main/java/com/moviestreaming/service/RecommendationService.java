package com.moviestreaming.service;

import com.moviestreaming.model.Movie;
import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.repository.MovieRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Intelligent Recommendation Engine combining Content-Based Similarity,
 * User Profile Affinity Scoring, and Cold-Start Trending Fallbacks.
 */
public class RecommendationService {

    private static final double WEIGHT_CATEGORY = 0.40;
    private static final double WEIGHT_DIRECTOR = 0.25;
    private static final double WEIGHT_ACTOR = 0.20;
    private static final double WEIGHT_RATING = 0.15;

    private final MovieRepository movieRepository;
    private final WatchHistoryService historyService;
    private final FavoriteService favoriteService;
    private final WatchlistService watchlistService;

    public RecommendationService(MovieRepository movieRepository, WatchHistoryService historyService,
                                 FavoriteService favoriteService, WatchlistService watchlistService) {
        if (movieRepository == null || historyService == null || favoriteService == null || watchlistService == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.movieRepository = movieRepository;
        this.historyService = historyService;
        this.favoriteService = favoriteService;
        this.watchlistService = watchlistService;
    }

    /**
     * Generates personalized movie recommendations for a user.
     * Falls back to top trending movies if user has no viewing or preference history.
     *
     * @param userId the user ID
     * @param limit  maximum number of recommendations to return
     * @return list of recommended Movie entities
     */
    public List<Movie> getRecommendationsForUser(String userId, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        List<Movie> favorites = favoriteService.getFavorites(userId);
        List<WatchHistoryItem> history = historyService.getHistory(userId);
        List<Movie> watchlist = watchlistService.getWatchlist(userId);

        Set<String> excludedMovieIds = new HashSet<>();
        for (WatchHistoryItem h : history) {
            if (h.isCompleted() || h.getWatchedDurationMinutes() > 10) {
                excludedMovieIds.add(h.getMovieId());
            }
        }

        // Cold-Start: No interactions -> return trending movies
        if (favorites.isEmpty() && history.isEmpty() && watchlist.isEmpty()) {
            return getTrendingMovies(limit, excludedMovieIds);
        }

        // 1. Build User Affinity Vector
        Map<String, Double> categoryWeights = new HashMap<>();
        Map<String, Double> directorWeights = new HashMap<>();
        Map<String, Double> actorWeights = new HashMap<>();

        // Process Favorites (Weight: 3.0)
        for (Movie m : favorites) {
            accumulatePreferences(m, 3.0, categoryWeights, directorWeights, actorWeights);
        }

        // Process Watchlist (Weight: 2.0)
        for (Movie m : watchlist) {
            accumulatePreferences(m, 2.0, categoryWeights, directorWeights, actorWeights);
        }

        // Process History (Weight: 1.0 - 2.0 based on completion)
        for (WatchHistoryItem h : history) {
            movieRepository.findById(h.getMovieId()).ifPresent(m -> {
                double weight = h.isCompleted() ? 2.0 : 1.0;
                accumulatePreferences(m, weight, categoryWeights, directorWeights, actorWeights);
            });
        }

        // 2. Score candidate unseen movies
        List<Movie> allMovies = movieRepository.findAll();
        List<ScoredMovie> scoredMovies = new ArrayList<>();

        for (Movie movie : allMovies) {
            if (excludedMovieIds.contains(movie.getId())) {
                continue;
            }

            double score = computeAffinityScore(movie, categoryWeights, directorWeights, actorWeights);
            scoredMovies.add(new ScoredMovie(movie, score));
        }

        // 3. Rank descending by score
        scoredMovies.sort(Comparator.comparingDouble(ScoredMovie::getScore).reversed()
                .thenComparing((sm) -> sm.getMovie().getRating(), Comparator.reverseOrder()));

        return scoredMovies.stream()
                .limit(limit)
                .map(ScoredMovie::getMovie)
                .collect(Collectors.toList());
    }

    /**
     * Finds movies most similar to a given target movie based on content metadata.
     *
     * @param movieId the target movie ID
     * @param limit   maximum number of similar movies
     * @return list of similar Movie entities
     */
    public List<Movie> getSimilarMovies(String movieId, int limit) {
        if (movieId == null || limit <= 0) {
            return Collections.emptyList();
        }

        Movie target = movieRepository.findById(movieId).orElse(null);
        if (target == null) {
            return Collections.emptyList();
        }

        List<Movie> allMovies = movieRepository.findAll();
        List<ScoredMovie> similarList = new ArrayList<>();

        Set<String> targetActors = new HashSet<>(target.getActors());

        for (Movie candidate : allMovies) {
            if (candidate.getId().equals(target.getId())) {
                continue;
            }

            double similarity = 0.0;

            // Genre match (+4.0)
            if (candidate.getCategoryId() != null && candidate.getCategoryId().equalsIgnoreCase(target.getCategoryId())) {
                similarity += 4.0;
            }

            // Director match (+3.0)
            if (candidate.getDirector() != null && candidate.getDirector().equalsIgnoreCase(target.getDirector())) {
                similarity += 3.0;
            }

            // Actors overlap (+1.5 per actor)
            for (String actor : candidate.getActors()) {
                if (targetActors.contains(actor)) {
                    similarity += 1.5;
                }
            }

            // Rating proximity
            double ratingDiff = Math.abs(candidate.getRating() - target.getRating());
            similarity += Math.max(0.0, 1.0 - (ratingDiff / 10.0));

            // Year proximity
            int yearDiff = Math.abs(candidate.getReleaseYear() - target.getReleaseYear());
            similarity += Math.max(0.0, 1.0 - (Math.min(20, yearDiff) / 20.0));

            similarList.add(new ScoredMovie(candidate, similarity));
        }

        similarList.sort(Comparator.comparingDouble(ScoredMovie::getScore).reversed()
                .thenComparing((sm) -> sm.getMovie().getRating(), Comparator.reverseOrder()));

        return similarList.stream()
                .limit(limit)
                .map(ScoredMovie::getMovie)
                .collect(Collectors.toList());
    }

    /**
     * Computes top trending movies based on view count, favorites, and rating.
     *
     * @param limit       number of results
     * @param excludedIds IDs to exclude
     * @return list of trending movies
     */
    public List<Movie> getTrendingMovies(int limit, Set<String> excludedIds) {
        return movieRepository.findAll().stream()
                .filter(m -> excludedIds == null || !excludedIds.contains(m.getId()))
                .sorted((m1, m2) -> {
                    double score1 = calculatePopularityScore(m1);
                    double score2 = calculatePopularityScore(m2);
                    return Double.compare(score2, score1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    private double calculatePopularityScore(Movie m) {
        return (m.getViewCount() * 0.5) + (m.getFavoriteCount() * 3.0) + (m.getRating() * 20.0);
    }

    private void accumulatePreferences(Movie movie, double weight,
                                       Map<String, Double> catMap,
                                       Map<String, Double> dirMap,
                                       Map<String, Double> actMap) {
        if (movie.getCategoryId() != null) {
            catMap.merge(movie.getCategoryId().toLowerCase(), weight, Double::sum);
        }
        if (movie.getDirector() != null) {
            dirMap.merge(movie.getDirector().toLowerCase(), weight, Double::sum);
        }
        if (movie.getActors() != null) {
            for (String actor : movie.getActors()) {
                actMap.merge(actor.toLowerCase(), weight, Double::sum);
            }
        }
    }

    private double computeAffinityScore(Movie movie,
                                        Map<String, Double> catMap,
                                        Map<String, Double> dirMap,
                                        Map<String, Double> actMap) {
        double score = 0.0;

        if (movie.getCategoryId() != null) {
            score += catMap.getOrDefault(movie.getCategoryId().toLowerCase(), 0.0) * WEIGHT_CATEGORY;
        }

        if (movie.getDirector() != null) {
            score += dirMap.getOrDefault(movie.getDirector().toLowerCase(), 0.0) * WEIGHT_DIRECTOR;
        }

        if (movie.getActors() != null) {
            double actorScore = 0.0;
            for (String actor : movie.getActors()) {
                actorScore += actMap.getOrDefault(actor.toLowerCase(), 0.0);
            }
            score += actorScore * WEIGHT_ACTOR;
        }

        score += (movie.getRating() / 10.0) * WEIGHT_RATING * 5.0;

        return score;
    }

    private static class ScoredMovie {
        private final Movie movie;
        private final double score;

        ScoredMovie(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }

        public Movie getMovie() {
            return movie;
        }

        public double getScore() {
            return score;
        }
    }
}
