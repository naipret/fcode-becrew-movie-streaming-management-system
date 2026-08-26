package com.moviestreaming.controller;

import com.moviestreaming.exception.AppException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.User;
import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.service.CategoryService;
import com.moviestreaming.service.FavoriteService;
import com.moviestreaming.service.MovieService;
import com.moviestreaming.service.RecommendationService;
import com.moviestreaming.service.SearchService;
import com.moviestreaming.service.SortOption;
import com.moviestreaming.service.SortingService;
import com.moviestreaming.service.UserSession;
import com.moviestreaming.service.WatchHistoryService;
import com.moviestreaming.service.WatchlistService;
import com.moviestreaming.util.InputHelper;
import com.moviestreaming.view.BannerView;
import com.moviestreaming.view.ConsoleTable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Controller handling Viewer/User interactions, catalog exploration, search, watchlist,
 * favorites, streaming playback simulation, and recommendation feeds.
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
    private final MovieController movieController;
    private final UserSession userSession;

    public UserController(MovieService movieService, CategoryService categoryService,
                          SearchService searchService, SortingService sortingService,
                          WatchlistService watchlistService, FavoriteService favoriteService,
                          WatchHistoryService historyService, RecommendationService recommendationService,
                          MovieController movieController, UserSession userSession) {
        this.movieService = movieService;
        this.categoryService = categoryService;
        this.searchService = searchService;
        this.sortingService = sortingService;
        this.watchlistService = watchlistService;
        this.favoriteService = favoriteService;
        this.historyService = historyService;
        this.recommendationService = recommendationService;
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
            System.out.println("3. Top Picks For You (Personalized Recommendations)");
            System.out.println("4. My Watchlist");
            System.out.println("5. My Favorites");
            System.out.println("6. Watch Movie (Streaming Simulation)");
            System.out.println("7. My Viewing History");
            System.out.println("0. Return to Main Menu");

            int choice = InputHelper.readInt(scanner, "Select an option [0-7]: ", 0, 7);
            switch (choice) {
                case 1:
                    handleBrowseCatalog(scanner);
                    break;
                case 2:
                    handleSearch(scanner);
                    break;
                case 3:
                    handleRecommendations(scanner, user.getId());
                    break;
                case 4:
                    handleWatchlist(scanner, user.getId());
                    break;
                case 5:
                    handleFavorites(scanner, user.getId());
                    break;
                case 6:
                    handleWatchMovie(scanner, user.getId());
                    break;
                case 7:
                    handleHistory(user.getId());
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
        BannerView.printSectionHeader("My Watchlist");
        List<Movie> watchlist = watchlistService.getWatchlist(userId);
        movieController.displayMovieTable(watchlist);

        System.out.println("\n1. Add Movie to Watchlist");
        System.out.println("2. Remove Movie from Watchlist");
        System.out.println("0. Back");

        int choice = InputHelper.readInt(scanner, "Option [0-2]: ", 0, 2);
        if (choice == 1) {
            String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to add: ");
            try {
                watchlistService.addToWatchlist(userId, id);
                BannerView.printSuccess("Movie added to watchlist!");
            } catch (AppException e) {
                BannerView.printError(e.getMessage());
            }
        } else if (choice == 2) {
            String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to remove: ");
            watchlistService.removeFromWatchlist(userId, id);
            BannerView.printSuccess("Movie removed from watchlist.");
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
        BannerView.printSectionHeader("Watch Movie (Streaming Player)");
        String movieId = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to stream: ");
        Optional<Movie> movieOpt = movieService.getMovieById(movieId);
        if (!movieOpt.isPresent()) {
            BannerView.printError("Movie not found.");
            return;
        }

        Movie movie = movieOpt.get();
        movieController.displayMovieDetail(movieId);

        System.out.println("\nStreaming playback started...");
        int minutes = InputHelper.readInt(scanner,
                "Enter watched duration in minutes [1-" + movie.getDurationMinutes() + "]: ",
                1, movie.getDurationMinutes());

        try {
            WatchHistoryItem item = historyService.recordWatchSession(userId, movieId, minutes);
            if (item.isCompleted()) {
                BannerView.printSuccess("Congratulations! You completed watching '" + movie.getTitle() + "'! 🎉");
            } else {
                BannerView.printInfo("Progress saved: " + minutes + " / " + movie.getDurationMinutes() + " mins.");
            }
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void handleHistory(String userId) {
        BannerView.printSectionHeader("My Watch History");
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
