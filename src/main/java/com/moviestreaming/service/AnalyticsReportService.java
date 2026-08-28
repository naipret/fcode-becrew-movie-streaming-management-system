package com.moviestreaming.service;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.User;
import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import com.moviestreaming.util.AtomicFileWriter;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service calculating analytics, trending categories within sliding time-windows, viewing habits,
 * and generating exportable reports.
 */
public class AnalyticsReportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WatchHistoryRepository historyRepository;
    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AnalyticsReportService(WatchHistoryRepository historyRepository,
            MovieRepository movieRepository, CategoryRepository categoryRepository,
            UserRepository userRepository) {
        if (historyRepository == null || movieRepository == null || categoryRepository == null
                || userRepository == null) {
            throw new IllegalArgumentException("Repositories must not be null");
        }
        this.historyRepository = historyRepository;
        this.movieRepository = movieRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * DTO representing a category paired with its watch count in a given time-window.
     */
    public static class TrendingCategory {
        private final Category category;
        private final long watchCount;

        public TrendingCategory(Category category, long watchCount) {
            this.category = category;
            this.watchCount = watchCount;
        }

        public Category getCategory() {
            return category;
        }

        public long getWatchCount() {
            return watchCount;
        }
    }

    /**
     * DTO representing aggregate viewing metrics for a single user.
     */
    public static class UserViewingStats {
        private final String userId;
        private final String username;
        private final int totalWatched;
        private final int totalCompleted;
        private final long totalMinutesWatched;
        private final double completionRate;
        private final String topCategoryName;

        public UserViewingStats(String userId, String username, int totalWatched,
                int totalCompleted, long totalMinutesWatched, double completionRate,
                String topCategoryName) {
            this.userId = userId;
            this.username = username;
            this.totalWatched = totalWatched;
            this.totalCompleted = totalCompleted;
            this.totalMinutesWatched = totalMinutesWatched;
            this.completionRate = completionRate;
            this.topCategoryName = topCategoryName;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public int getTotalWatched() {
            return totalWatched;
        }

        public int getTotalCompleted() {
            return totalCompleted;
        }

        public long getTotalMinutesWatched() {
            return totalMinutesWatched;
        }

        public double getCompletionRate() {
            return completionRate;
        }

        public String getTopCategoryName() {
            return topCategoryName;
        }

        public String getFormattedHours() {
            long hours = totalMinutesWatched / 60;
            long mins = totalMinutesWatched % 60;
            return hours + "h " + mins + "m";
        }
    }

    /**
     * DTO representing system-wide overview statistics.
     */
    public static class SystemStats {
        private final int totalMovies;
        private final int totalCategories;
        private final int totalUsers;
        private final int totalSessions;
        private final long totalWatchMinutes;
        private final double averageRating;

        public SystemStats(int totalMovies, int totalCategories, int totalUsers, int totalSessions,
                long totalWatchMinutes, double averageRating) {
            this.totalMovies = totalMovies;
            this.totalCategories = totalCategories;
            this.totalUsers = totalUsers;
            this.totalSessions = totalSessions;
            this.totalWatchMinutes = totalWatchMinutes;
            this.averageRating = averageRating;
        }

        public int getTotalMovies() {
            return totalMovies;
        }

        public int getTotalCategories() {
            return totalCategories;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public long getTotalWatchMinutes() {
            return totalWatchMinutes;
        }

        public double getAverageRating() {
            return averageRating;
        }
    }

    /**
     * Computes trending categories by counting watch history entries within the last N days.
     *
     * @param daysWindow time window in days (e.g. 7 or 30)
     * @param limit maximum categories to return
     * @return list of trending categories sorted descending by view count
     */
    public List<TrendingCategory> getTrendingCategories(int daysWindow, int limit) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysWindow > 0 ? daysWindow : 7);

        Map<String, Long> categoryCountMap = new HashMap<>();
        List<WatchHistoryItem> allHistory = historyRepository.findAll();

        for (WatchHistoryItem item : allHistory) {
            if (item.getLastWatchedTimestamp() != null
                    && item.getLastWatchedTimestamp().isAfter(threshold)) {
                Optional<Movie> movieOpt = movieRepository.findById(item.getMovieId());
                if (movieOpt.isPresent()) {
                    String catId = movieOpt.get().getCategoryId();
                    if (catId != null && !catId.isEmpty()) {
                        categoryCountMap.put(catId, categoryCountMap.getOrDefault(catId, 0L) + 1L);
                    }
                }
            }
        }

        List<TrendingCategory> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryCountMap.entrySet()) {
            Category cat = categoryRepository.findById(entry.getKey()).orElse(null);
            if (cat != null) {
                result.add(new TrendingCategory(cat, entry.getValue()));
            }
        }

        result.sort((a, b) -> Long.compare(b.getWatchCount(), a.getWatchCount()));

        if (limit > 0 && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    /**
     * Computes user viewing statistics.
     *
     * @param userId user ID
     * @return UserViewingStats
     */
    public UserViewingStats getUserViewingStats(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        User user = userRepository.findById(userId.trim())
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        List<WatchHistoryItem> userHistory = historyRepository.findByUserId(user.getId());

        int totalWatched = userHistory.size();
        int totalCompleted = 0;
        long totalMinutes = 0;
        Map<String, Integer> genreCount = new HashMap<>();

        for (WatchHistoryItem item : userHistory) {
            totalMinutes += item.getWatchedDurationMinutes();
            if (item.isCompleted()) {
                totalCompleted++;
            }
            movieRepository.findById(item.getMovieId()).ifPresent(movie -> {
                String catId = movie.getCategoryId();
                if (catId != null) {
                    genreCount.put(catId, genreCount.getOrDefault(catId, 0) + 1);
                }
            });
        }

        double completionRate =
                totalWatched > 0 ? ((double) totalCompleted / totalWatched) * 100.0 : 0.0;
        completionRate = Math.round(completionRate * 10.0) / 10.0;

        String topCatName = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topCatName = categoryRepository.findById(entry.getKey()).map(Category::getName)
                        .orElse(entry.getKey());
            }
        }

        return new UserViewingStats(user.getId(), user.getUsername(), totalWatched, totalCompleted,
                totalMinutes, completionRate, topCatName);
    }

    /**
     * Computes system-wide aggregate metrics.
     *
     * @return SystemStats
     */
    public SystemStats getSystemOverviewStats() {
        List<Movie> movies = movieRepository.findAll();
        int totalMovies = movies.size();
        int totalCategories = categoryRepository.findAll().size();
        int totalUsers = userRepository.findAll().size();

        List<WatchHistoryItem> history = historyRepository.findAll();
        int totalSessions = history.size();
        long totalMinutes =
                history.stream().mapToLong(WatchHistoryItem::getWatchedDurationMinutes).sum();

        double avgRating = movies.stream().mapToDouble(Movie::getRating).average().orElse(0.0);
        avgRating = Math.round(avgRating * 100.0) / 100.0;

        return new SystemStats(totalMovies, totalCategories, totalUsers, totalSessions,
                totalMinutes, avgRating);
    }

    /**
     * Exports a comprehensive viewing report for a user to a formatted text file.
     *
     * @param userId user ID
     * @param destinationDirPath target directory path (e.g. "reports")
     * @return path of the exported report file
     */
    public String exportUserViewingReport(String userId, String destinationDirPath) {
        UserViewingStats stats = getUserViewingStats(userId);
        User user = userRepository.findById(userId.trim()).orElse(null);
        String name = user != null ? user.getFullName() : userId;

        List<WatchHistoryItem> userHistory = historyRepository.findByUserId(userId.trim());

        List<String> lines = new ArrayList<>();
        lines.add(
                "================================================================================");
        lines.add(
                "                   NETFLIX CLI - USER VIEWING ANALYTICS REPORT                   ");
        lines.add(
                "================================================================================");
        lines.add("Generated At : " + LocalDateTime.now().format(DATE_FORMATTER));
        lines.add("User ID      : " + stats.getUserId());
        lines.add("Username     : " + stats.getUsername() + " (" + name + ")");
        lines.add(
                "--------------------------------------------------------------------------------");
        lines.add("SUMMARY METRICS");
        lines.add("Total Movies Watched   : " + stats.getTotalWatched());
        lines.add("Movies Completed       : " + stats.getTotalCompleted());
        lines.add("Completion Rate        : " + stats.getCompletionRate() + "%");
        lines.add("Total Viewing Time     : " + stats.getFormattedHours() + " ("
                + stats.getTotalMinutesWatched() + " minutes)");
        lines.add("Top Preferred Category : " + stats.getTopCategoryName());
        lines.add(
                "--------------------------------------------------------------------------------");
        lines.add("CHRONOLOGICAL VIEWING HISTORY");
        lines.add(String.format("%-10s | %-30s | %-12s | %-10s | %-19s", "Movie ID", "Title",
                "Progress", "Status", "Last Watched"));
        lines.add(
                "--------------------------------------------------------------------------------");

        for (WatchHistoryItem item : userHistory) {
            String title = movieRepository.findById(item.getMovieId()).map(Movie::getTitle)
                    .orElse("Unknown Movie");
            if (title.length() > 30) {
                title = title.substring(0, 27) + "...";
            }
            String progress =
                    item.getWatchedDurationMinutes() + "/" + item.getTotalDurationMinutes() + "m";
            String status = item.isCompleted() ? "[DONE]" : "[IN-PROGRESS]";
            String watchedTime = item.getLastWatchedTimestamp() != null
                    ? item.getLastWatchedTimestamp().format(DATE_FORMATTER)
                    : "N/A";

            lines.add(String.format("%-10s | %-30s | %-12s | %-10s | %-19s", item.getMovieId(),
                    title, progress, status, watchedTime));
        }

        lines.add(
                "================================================================================");
        lines.add("End of Report. Thank you for streaming with Netflix CLI!");
        lines.add(
                "================================================================================");

        String dir = (destinationDirPath != null && !destinationDirPath.trim().isEmpty())
                ? destinationDirPath.trim()
                : AppConstants.REPORTS_DIR;
        String filePath = dir + File.separator + "viewing_report_" + userId.trim() + ".txt";

        AtomicFileWriter.writeLines(filePath, lines);
        return filePath;
    }

    /**
     * Exports user viewing history to a structured CSV report.
     *
     * @param userId user ID
     * @param destinationDirPath target directory path
     * @return path of the exported CSV file
     */
    public String exportUserViewingReportCsv(String userId, String destinationDirPath) {
        User user = userRepository.findById(userId.trim())
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        List<WatchHistoryItem> userHistory = historyRepository.findByUserId(user.getId());

        List<String> lines = new ArrayList<>();
        lines.add(
                "historyId|movieId|title|category|watchedMinutes|totalMinutes|isCompleted|lastWatched");

        for (WatchHistoryItem item : userHistory) {
            Optional<Movie> movieOpt = movieRepository.findById(item.getMovieId());
            String title = movieOpt.map(Movie::getTitle).orElse("Unknown");
            String catId = movieOpt.map(Movie::getCategoryId).orElse("N/A");
            String catName =
                    categoryRepository.findById(catId).map(Category::getName).orElse(catId);
            String timestamp = item.getLastWatchedTimestamp() != null
                    ? item.getLastWatchedTimestamp().toString()
                    : "";

            lines.add(String.format("%s|%s|%s|%s|%d|%d|%s|%s", item.getId(), item.getMovieId(),
                    title, catName, item.getWatchedDurationMinutes(),
                    item.getTotalDurationMinutes(), item.isCompleted(), timestamp));
        }

        String dir = (destinationDirPath != null && !destinationDirPath.trim().isEmpty())
                ? destinationDirPath.trim()
                : AppConstants.REPORTS_DIR;
        String filePath = dir + File.separator + "viewing_report_" + user.getId() + ".csv";

        AtomicFileWriter.writeLines(filePath, lines);
        return filePath;
    }
}
