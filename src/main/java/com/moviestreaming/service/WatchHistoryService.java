package com.moviestreaming.service;

import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing user playback tracking, completion calculations, and movie view count increments.
 */
public class WatchHistoryService {

    private static final double COMPLETION_THRESHOLD = 0.90; // 90% duration considered completed

    private final WatchHistoryRepository historyRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    public WatchHistoryService(WatchHistoryRepository historyRepository, MovieRepository movieRepository,
                               MovieService movieService) {
        if (historyRepository == null || movieRepository == null || movieService == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.historyRepository = historyRepository;
        this.movieRepository = movieRepository;
        this.movieService = movieService;
    }

    /**
     * Records a viewing session for a user and increments the movie view count.
     *
     * @param userId         the user ID
     * @param movieId        the movie ID
     * @param watchedMinutes number of minutes watched
     * @return the saved WatchHistoryItem
     */
    public WatchHistoryItem recordWatchSession(String userId, String movieId, int watchedMinutes) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("User ID cannot be empty.");
        }
        if (movieId == null || movieId.trim().isEmpty()) {
            throw new ValidationException("Movie ID cannot be empty.");
        }
        if (watchedMinutes <= 0) {
            throw new ValidationException("Watched minutes must be greater than zero.");
        }

        Movie movie = movieRepository.findById(movieId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Movie", movieId));

        boolean completed = watchedMinutes >= (int) (movie.getDurationMinutes() * COMPLETION_THRESHOLD);

        // Find existing history record for this user and movie if present
        Optional<WatchHistoryItem> existing = historyRepository.findByUserId(userId.trim()).stream()
                .filter(h -> h.getMovieId() != null && h.getMovieId().equals(movieId.trim()))
                .findFirst();

        WatchHistoryItem item;
        if (existing.isPresent()) {
            item = existing.get();
            item.setWatchedDurationMinutes(watchedMinutes);
            item.setTotalDurationMinutes(movie.getDurationMinutes());
            item.setLastWatchedTimestamp(LocalDateTime.now());
            item.setCompleted(item.isCompleted() || completed);
        } else {
            String nextId = generateNextId();
            item = new WatchHistoryItem(
                    nextId,
                    userId.trim(),
                    movieId.trim(),
                    watchedMinutes,
                    movie.getDurationMinutes(),
                    LocalDateTime.now(),
                    completed
            );
        }

        WatchHistoryItem saved = historyRepository.save(item);

        // Increment movie view counter
        movieService.incrementViewCount(movieId.trim());

        return saved;
    }

    /**
     * Retrieves watch history records for a user, sorted newest first.
     *
     * @param userId the user ID
     * @return list of WatchHistoryItem entities
     */
    public List<WatchHistoryItem> getHistory(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return historyRepository.findByUserId(userId.trim()).stream()
                .sorted(Comparator.comparing(WatchHistoryItem::getLastWatchedTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Clears all watch history for a specific user.
     *
     * @param userId the user ID
     */
    public void clearHistory(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        List<WatchHistoryItem> userHistory = historyRepository.findByUserId(userId.trim());
        for (WatchHistoryItem item : userHistory) {
            historyRepository.deleteById(item.getId());
        }
    }

    private synchronized String generateNextId() {
        int maxIndex = historyRepository.findAll().stream()
                .map(WatchHistoryItem::getId)
                .filter(id -> id != null && id.startsWith("HIS-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(4));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("HIS-%03d", maxIndex + 1);
    }
}
