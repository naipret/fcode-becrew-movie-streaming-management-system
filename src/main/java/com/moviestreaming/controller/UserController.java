package com.moviestreaming.controller;

import com.moviestreaming.command.AddToWatchlistCommand;
import com.moviestreaming.command.ClearWatchlistCommand;
import com.moviestreaming.command.RemoveFromWatchlistCommand;
import com.moviestreaming.command.WatchlistCommand;
import com.moviestreaming.exception.AppException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.User;
import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.service.AnalyticsReportService;
import com.moviestreaming.service.CategoryService;
import com.moviestreaming.service.FavoriteService;
import com.moviestreaming.service.FilterService;
import com.moviestreaming.service.MovieRankingEngine;
import com.moviestreaming.service.MovieService;
import com.moviestreaming.service.RecommendationService;
import com.moviestreaming.service.SearchService;
import com.moviestreaming.service.SortOption;
import com.moviestreaming.service.SortingService;
import com.moviestreaming.service.UserSession;
import com.moviestreaming.service.WatchHistoryService;
import com.moviestreaming.service.WatchlistService;
import com.moviestreaming.service.WatchlistUndoRedoService;
import com.moviestreaming.specification.MovieFilterCriteria;
import com.moviestreaming.util.InputHelper;
import com.moviestreaming.view.BannerView;
import com.moviestreaming.view.ConsoleTable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Controller handling Viewer/User interactions, catalog exploration, search, watchlist (with Undo/Redo),
 * favorites, streaming playback simulation, rankings, filtering, and analytics reports.
 */
public class UserController {

    private final MovieService movieService;
    private final CategoryService categoryService;
    private final SearchService searchService;
    private final SortingService sortingService;
    private final WatchlistService watchlistService;
    private final FavoriteService favoriteService;
    private final WatchHistoryService historyService;
    private final RecommendationService recommendationService;
    private final WatchlistUndoRedoService undoRedoService;
    private final FilterService filterService;
    private final MovieRankingEngine rankingEngine;
    private final AnalyticsReportService analyticsReportService;
    private final MovieController movieController;
    private final UserSession userSession;

    public UserController(MovieService movieService, CategoryService categoryService,
                          SearchService searchService, SortingService sortingService,
                          WatchlistService watchlistService, FavoriteService favoriteService,
                          WatchHistoryService historyService, RecommendationService recommendationService,
                          MovieController movieController, UserSession userSession) {
        this(movieService, categoryService, searchService, sortingService,
                watchlistService, favoriteService, historyService, recommendationService,
                new WatchlistUndoRedoService(), null, null, null,
                movieController, userSession);
    }

    public UserController(MovieService movieService, CategoryService categoryService,
                          SearchService searchService, SortingService sortingService,
                          WatchlistService watchlistService, FavoriteService favoriteService,
                          WatchHistoryService historyService, RecommendationService recommendationService,
                          WatchlistUndoRedoService undoRedoService, FilterService filterService,
                          MovieRankingEngine rankingEngine, AnalyticsReportService analyticsReportService,
                          MovieController movieController, UserSession userSession) {
        this.movieService = movieService;
        this.categoryService = categoryService;
        this.searchService = searchService;
        this.sortingService = sortingService;
        this.watchlistService = watchlistService;
        this.favoriteService = favoriteService;
        this.historyService = historyService;
        this.recommendationService = recommendationService;
        this.undoRedoService = undoRedoService != null ? undoRedoService : new WatchlistUndoRedoService();
        this.filterService = filterService;
        this.rankingEngine = rankingEngine;
        this.analyticsReportService = analyticsReportService;
        this.movieController = movieController;
        this.userSession = userSession;
    }

    /**
     * Executes the Viewer interactive dashboard loop.
     *
     * @param scanner console scanner
     */
    public void runUserMenu(Scanner scanner) {
        if (!userSession.isLoggedIn()) {
            BannerView.printError("Please log in to access the viewer dashboard.");
            return;
        }

        User user = userSession.getCurrentUser().orElse(null);
        boolean running = true;
        while (running) {
            BannerView.printSectionHeader("Netflix Viewer Dashboard");
            BannerView.printUserBadge(user);
            System.out.println("1. Browse Movies (Catalog & Sorting)");
            System.out.println("2. Search Movies (Multi-Field / Universal)");
            System.out.println("3. Advanced Multi-Condition Filter (Specification Pattern)");
            System.out.println("4. Top Ranked Movies (Weighted Max-Heap Leaderboard)");
            System.out.println("5. Top Picks For You (Personalized Recommendations)");
            System.out.println("6. My Watchlist (With Undo / Redo)");
            System.out.println("7. My Favorites");
            System.out.println("8. Watch Movie (Streaming Simulation)");
            System.out.println("9. My Viewing History & Analytics Report");
            System.out.println("0. Return to Main Menu");

            int choice = InputHelper.readInt(scanner, "Select an option [0-9]: ", 0, 9);
            switch (choice) {
                case 1:
                    handleBrowseCatalog(scanner);
                    break;
                case 2:
                    handleSearch(scanner);
                    break;
                case 3:
                    handleAdvancedFilter(scanner);
                    break;
                case 4:
                    handleTopRanked(scanner);
                    break;
                case 5:
                    handleRecommendations(scanner, user.getId());
                    break;
                case 6:
                    handleWatchlist(scanner, user.getId());
                    break;
                case 7:
                    handleFavorites(scanner, user.getId());
                    break;
                case 8:
                    handleWatchMovie(scanner, user.getId());
                    break;
                case 9:
                    handleHistoryAndAnalytics(scanner, user.getId());
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void handleBrowseCatalog(Scanner scanner) {
        BannerView.printSectionHeader("Browse Catalog");
        System.out.println("1. View All Movies (Default)");
        System.out.println("2. Filter By Genre / Category");
        System.out.println("3. Sort by Rating (Highest First)");
        System.out.println("4. Sort by Popularity (Most Viewed)");
        System.out.println("5. Sort by Release Year (Newest First)");

        int choice = InputHelper.readInt(scanner, "Choose view [1-5]: ", 1, 5);
        List<Movie> movies;

        if (choice == 2) {
            System.out.println("Available Categories: ");
            for (Category c : categoryService.getAllCategories()) {
                System.out.println("[" + c.getId() + "] " + c.getName());
            }
            String catId = InputHelper.readNonEmptyString(scanner, "Enter Category ID: ");
            movies = movieService.getMoviesByCategory(catId);
        } else if (choice == 3) {
            movies = sortingService.sort(movieService.getAllMovies(), SortOption.RATING_DESC);
        } else if (choice == 4) {
            movies = sortingService.sort(movieService.getAllMovies(), SortOption.POPULARITY_DESC);
        } else if (choice == 5) {
            movies = sortingService.sort(movieService.getAllMovies(), SortOption.RELEASE_YEAR_DESC);
        } else {
            movies = movieService.getAllMovies();
        }

        movieController.displayMovieTable(movies);
        promptForDetail(scanner);
    }

    private void handleSearch(Scanner scanner) {
        BannerView.printSectionHeader("Search Movies");
        System.out.println("1. Universal Keyword Search (All Fields)");
        System.out.println("2. Search By Title");
        System.out.println("3. Search By Actor");
        System.out.println("4. Search By Director");
        System.out.println("5. Search By Genre");

        int choice = InputHelper.readInt(scanner, "Choose search mode [1-5]: ", 1, 5);
        String query = InputHelper.readNonEmptyString(scanner, "Enter search keyword: ");
        List<Movie> results;

        switch (choice) {
            case 2:
                results = searchService.searchByTitle(query);
                break;
            case 3:
                results = searchService.searchByActor(query);
                break;
            case 4:
                results = searchService.searchByDirector(query);
                break;
            case 5:
                results = searchService.searchByGenre(query);
                break;
            case 1:
            default:
                results = searchService.searchUniversal(query);
                break;
        }

        movieController.displayMovieTable(results);
        promptForDetail(scanner);
    }

    private void handleAdvancedFilter(Scanner scanner) {
        BannerView.printSectionHeader("Advanced Multi-Condition Filter (Specification Pattern)");
        if (filterService == null) {
            BannerView.printInfo("Filter service is initializing.");
            return;
        }

        MovieFilterCriteria criteria = new MovieFilterCriteria();

        System.out.println("Configure Filter Conditions (Press Enter to skip any condition):");

        System.out.println("Available Categories:");
        for (Category c : categoryService.getAllCategories()) {
            System.out.print("[" + c.getId() + ": " + c.getName() + "] ");
        }
        System.out.println();
        System.out.print("Category ID (or Enter to skip): ");
        String catInput = scanner.nextLine().trim();
        if (!catInput.isEmpty()) {
            criteria.setCategoryId(catInput);
        }

        System.out.print("Minimum Rating 0.0-10.0 (or Enter to skip): ");
        String ratingInput = scanner.nextLine().trim();
        if (!ratingInput.isEmpty()) {
            try {
                criteria.setMinRating(Double.parseDouble(ratingInput));
            } catch (NumberFormatException ignored) {
                // Skip invalid rating
            }
        }

        System.out.print("From Year (or Enter to skip): ");
        String fromYearInput = scanner.nextLine().trim();
        if (!fromYearInput.isEmpty()) {
            try {
                criteria.setFromYear(Integer.parseInt(fromYearInput));
            } catch (NumberFormatException ignored) {
                // Skip invalid year
            }
        }

        System.out.print("To Year (or Enter to skip): ");
        String toYearInput = scanner.nextLine().trim();
        if (!toYearInput.isEmpty()) {
            try {
                criteria.setToYear(Integer.parseInt(toYearInput));
            } catch (NumberFormatException ignored) {
                // Skip invalid year
            }
        }

        System.out.print("Actor Name (or Enter to skip): ");
        String actorInput = scanner.nextLine().trim();
        if (!actorInput.isEmpty()) {
            criteria.setActorName(actorInput);
        }

        System.out.print("Director Name (or Enter to skip): ");
        String directorInput = scanner.nextLine().trim();
        if (!directorInput.isEmpty()) {
            criteria.setDirectorName(directorInput);
        }

        List<Movie> filtered = filterService.filter(criteria);
        BannerView.printSectionHeader("Filter Results (" + filtered.size() + " matches found)");
        movieController.displayMovieTable(filtered);
        promptForDetail(scanner);
    }

    private void handleTopRanked(Scanner scanner) {
        BannerView.printSectionHeader("Top Ranked Movies (Weighted Max-Heap Ranking)");
        if (rankingEngine == null) {
            BannerView.printInfo("Ranking Engine is initializing.");
            return;
        }

        List<MovieRankingEngine.RankedMovie> ranked = rankingEngine.rankMovies(10);
        if (ranked.isEmpty()) {
            BannerView.printInfo("No movies available to rank.");
            return;
        }

        ConsoleTable table = new ConsoleTable("Rank", "ID", "Title", "Score", "Rating", "Views", "Favorites");
        int rank = 1;
        for (MovieRankingEngine.RankedMovie rm : ranked) {
            Movie m = rm.getMovie();
            table.addRow(
                    "#" + rank++,
                    m.getId(),
                    m.getTitle(),
                    String.format("%.3f", rm.getScore()),
                    m.getRating() + " ⭐",
                    String.valueOf(m.getViewCount()),
                    String.valueOf(m.getFavoriteCount())
            );
        }
        table.print();
        promptForDetail(scanner);
    }

    private void handleRecommendations(Scanner scanner, String userId) {
        BannerView.printSectionHeader("Personalized Recommendations For You");
        List<Movie> recommendations = recommendationService.getRecommendationsForUser(userId, 5);
        movieController.displayMovieTable(recommendations);

        System.out.println("\nWould you like to find movies similar to a specific movie?");
        System.out.println("1. Find Similar Movies");
        System.out.println("0. Back");
        int subChoice = InputHelper.readInt(scanner, "Option: ", 0, 1);
        if (subChoice == 1) {
            String movieId = InputHelper.readNonEmptyString(scanner, "Enter Movie ID: ");
            List<Movie> similar = recommendationService.getSimilarMovies(movieId, 5);
            BannerView.printSectionHeader("Movies Similar to " + movieId);
            movieController.displayMovieTable(similar);
            promptForDetail(scanner);
        }
    }

    private void handleWatchlist(Scanner scanner, String userId) {
        boolean inWatchlistMenu = true;
        while (inWatchlistMenu) {
            BannerView.printSectionHeader("My Watchlist");
            List<Movie> watchlist = watchlistService.getWatchlist(userId);
            movieController.displayMovieTable(watchlist);

            System.out.println("\n--- Watchlist Actions (Command Pattern) ---");
            System.out.println("1. Add Movie to Watchlist");
            System.out.println("2. Remove Movie from Watchlist");
            System.out.println("3. Clear Entire Watchlist");
            System.out.println("4. ↩ Undo Last Action " + (undoRedoService.canUndo(userId) ? "(Available)" : "(Empty)"));
            System.out.println("5. ↪ Redo Last Action " + (undoRedoService.canRedo(userId) ? "(Available)" : "(Empty)"));
            System.out.println("6. View Undo / Redo History");
            System.out.println("0. Back");

            int choice = InputHelper.readInt(scanner, "Option [0-6]: ", 0, 6);
            switch (choice) {
                case 1:
                    String addId = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to add: ");
                    try {
                        String title = movieService.getMovieById(addId).map(Movie::getTitle).orElse(addId);
                        WatchlistCommand cmd = new AddToWatchlistCommand(watchlistService, userId, addId, title);
                        undoRedoService.execute(userId, cmd);
                        BannerView.printSuccess("Executed: " + cmd.getDescription());
                    } catch (AppException e) {
                        BannerView.printError(e.getMessage());
                    }
                    break;
                case 2:
                    String removeId = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to remove: ");
                    try {
                        String title = movieService.getMovieById(removeId).map(Movie::getTitle).orElse(removeId);
                        WatchlistCommand cmd = new RemoveFromWatchlistCommand(watchlistService, userId, removeId, title);
                        undoRedoService.execute(userId, cmd);
                        BannerView.printSuccess("Executed: " + cmd.getDescription());
                    } catch (AppException e) {
                        BannerView.printError(e.getMessage());
                    }
                    break;
                case 3:
                    if (watchlist.isEmpty()) {
                        BannerView.printInfo("Watchlist is already empty.");
                    } else {
                        WatchlistCommand cmd = new ClearWatchlistCommand(watchlistService, userId);
                        undoRedoService.execute(userId, cmd);
                        BannerView.printSuccess("Executed: " + cmd.getDescription());
                    }
                    break;
                case 4:
                    Optional<WatchlistCommand> undone = undoRedoService.undo(userId);
                    if (undone.isPresent()) {
                        BannerView.printSuccess("Undone: " + undone.get().getDescription());
                    } else {
                        BannerView.printInfo("Nothing to undo.");
                    }
                    break;
                case 5:
                    Optional<WatchlistCommand> redone = undoRedoService.redo(userId);
                    if (redone.isPresent()) {
                        BannerView.printSuccess("Redone: " + redone.get().getDescription());
                    } else {
                        BannerView.printInfo("Nothing to redo.");
                    }
                    break;
                case 6:
                    displayUndoRedoHistory(userId);
                    break;
                case 0:
                    inWatchlistMenu = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void displayUndoRedoHistory(String userId) {
        BannerView.printSectionHeader("Undo / Redo History");
        List<String> undoList = undoRedoService.getUndoHistory(userId);
        List<String> redoList = undoRedoService.getRedoHistory(userId);

        System.out.println("--- Undo Stack (Most Recent First) ---");
        if (undoList.isEmpty()) {
            System.out.println("  (Empty)");
        } else {
            for (int i = 0; i < undoList.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + undoList.get(i));
            }
        }

        System.out.println("\n--- Redo Stack (Ready to Redo) ---");
        if (redoList.isEmpty()) {
            System.out.println("  (Empty)");
        } else {
            for (int i = 0; i < redoList.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + redoList.get(i));
            }
        }
    }

    private void handleFavorites(Scanner scanner, String userId) {
        BannerView.printSectionHeader("My Favorites");
        List<Movie> favorites = favoriteService.getFavorites(userId);
        movieController.displayMovieTable(favorites);

        System.out.println("\n1. Add Movie to Favorites");
        System.out.println("2. Remove Movie from Favorites");
        System.out.println("0. Back");

        int choice = InputHelper.readInt(scanner, "Option [0-2]: ", 0, 2);
        if (choice == 1) {
            String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to favorite: ");
            try {
                favoriteService.addToFavorites(userId, id);
                BannerView.printSuccess("Movie added to favorites! ❤");
            } catch (AppException e) {
                BannerView.printError(e.getMessage());
            }
        } else if (choice == 2) {
            String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to unfavorite: ");
            favoriteService.removeFromFavorites(userId, id);
            BannerView.printSuccess("Movie removed from favorites.");
        }
    }

    private void handleWatchMovie(Scanner scanner, String userId) {
        BannerView.printSectionHeader("Watch Movie (Streaming Simulator)");
        String movieId = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to stream: ");
        Optional<Movie> movieOpt = movieService.getMovieById(movieId);
        if (!movieOpt.isPresent()) {
            BannerView.printError("Movie not found.");
            return;
        }

        Movie movie = movieOpt.get();
        movieController.displayMovieDetail(movieId);

        System.out.println("\nStreaming playback player started...");
        int minutes = InputHelper.readInt(scanner,
                "Enter watched duration in minutes [1-" + movie.getDurationMinutes() + "]: ",
                1, movie.getDurationMinutes());

        renderProgressBar(minutes, movie.getDurationMinutes());

        try {
            WatchHistoryItem item = historyService.recordWatchSession(userId, movieId, minutes);
            if (item.isCompleted()) {
                BannerView.printSuccess("Congratulations! You completed watching '" + movie.getTitle() + "'! 🎉");
            } else {
                BannerView.printInfo("Progress saved: " + minutes + " / " + movie.getDurationMinutes() + " mins (Continue Watching).");
            }
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void renderProgressBar(int current, int total) {
        int barLength = 30;
        double ratio = (double) current / total;
        int filled = (int) (ratio * barLength);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                sb.append("=");
            } else if (i == filled) {
                sb.append(">");
            } else {
                sb.append(" ");
            }
        }
        sb.append(String.format("] %d/%d mins (%.0f%%)", current, total, ratio * 100));
        System.out.println("\nPlayback Progress:");
        System.out.println(sb.toString());
        System.out.println();
    }

    private void handleHistoryAndAnalytics(Scanner scanner, String userId) {
        BannerView.printSectionHeader("My Viewing History & Analytics Report");
        List<WatchHistoryItem> history = historyService.getHistory(userId);
        if (history.isEmpty()) {
            BannerView.printInfo("No viewing history found.");
            return;
        }

        ConsoleTable table = new ConsoleTable("History ID", "Movie", "Watched", "Total", "Status", "Last Watched");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (WatchHistoryItem h : history) {
            String title = movieService.getMovieById(h.getMovieId()).map(Movie::getTitle).orElse(h.getMovieId());
            String status = h.isCompleted() ? "Completed ✅" : "Incomplete ⏳";
            String date = h.getLastWatchedTimestamp() != null ? h.getLastWatchedTimestamp().format(formatter) : "N/A";
            table.addRow(
                    h.getId(),
                    title,
                    h.getWatchedDurationMinutes() + "m",
                    h.getTotalDurationMinutes() + "m",
                    status,
                    date
            );
        }
        table.print();

        if (analyticsReportService != null) {
            AnalyticsReportService.UserViewingStats stats = analyticsReportService.getUserViewingStats(userId);
            System.out.println("\n--- Quick Analytics Summary ---");
            System.out.println("Total Movies Watched   : " + stats.getTotalWatched());
            System.out.println("Completion Rate        : " + stats.getCompletionRate() + "%");
            System.out.println("Total Time Watched     : " + stats.getFormattedHours());
            System.out.println("Top Favorite Genre     : " + stats.getTopCategoryName());

            System.out.println("\n1. Export Full Viewing Report (.txt)");
            System.out.println("2. Export CSV Viewing Report (.csv)");
            System.out.println("0. Back");

            int exportChoice = InputHelper.readInt(scanner, "Option [0-2]: ", 0, 2);
            if (exportChoice == 1) {
                try {
                    String path = analyticsReportService.exportUserViewingReport(userId, "reports");
                    BannerView.printSuccess("Report exported successfully to: " + path);
                } catch (AppException e) {
                    BannerView.printError("Failed to export report: " + e.getMessage());
                }
            } else if (exportChoice == 2) {
                try {
                    String path = analyticsReportService.exportUserViewingReportCsv(userId, "reports");
                    BannerView.printSuccess("CSV report exported successfully to: " + path);
                } catch (AppException e) {
                    BannerView.printError("Failed to export CSV: " + e.getMessage());
                }
            }
        }
    }

    private void promptForDetail(Scanner scanner) {
        System.out.println("\nWould you like to inspect a movie?");
        System.out.println("1. View Details Card");
        System.out.println("0. Continue");
        int c = InputHelper.readInt(scanner, "Choice: ", 0, 1);
        if (c == 1) {
            String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID: ");
            movieController.displayMovieDetail(id);
        }
    }
}
