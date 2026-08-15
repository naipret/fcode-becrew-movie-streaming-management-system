package com.moviestreaming.config;

/**
 * Application-wide constants defining storage paths, default configuration,
 * and ranking formula parameters.
 */
public final class AppConstants {

    // Application Metadata
    public static final String APP_NAME = "Netflix CLI - Movie Streaming Management System";
    public static final String APP_VERSION = "1.0.0";

    // Storage Paths
    public static final String DATA_DIR = "data";
    public static final String BACKUP_DIR = "data/backup";
    public static final String REPORTS_DIR = "reports";

    // File Names
    public static final String MOVIES_FILE = "movies.csv";
    public static final String CATEGORIES_FILE = "categories.csv";
    public static final String USERS_FILE = "users.csv";
    public static final String WATCH_HISTORY_FILE = "watch_history.csv";
    public static final String USER_WATCHLISTS_FILE = "user_watchlists.csv";
    public static final String USER_FAVORITES_FILE = "user_favorites.csv";

    // Delimiter and Encoding
    public static final String CSV_DELIMITER = "|";
    public static final String CSV_DELIMITER_REGEX = "\\|";
    public static final String DEFAULT_CHARSET = "UTF-8";

    // Ranking Weights (w_r + w_v + w_f = 1.0)
    public static final double WEIGHT_RATING = 0.40;
    public static final double WEIGHT_VIEWS = 0.35;
    public static final double WEIGHT_FAVORITES = 0.25;

    // Pagination & UI Limits
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_UNDO_REDO_STACK_SIZE = 30;

    private AppConstants() {
        // Utility class constructor
    }
}
