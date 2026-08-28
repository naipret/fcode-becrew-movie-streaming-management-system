package com.moviestreaming.controller;

import com.moviestreaming.exception.AppException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.service.AnalyticsReportService;
import com.moviestreaming.service.CategoryService;
import com.moviestreaming.service.MovieRankingEngine;
import com.moviestreaming.service.MovieService;
import com.moviestreaming.service.UserSession;
import com.moviestreaming.util.InputHelper;
import com.moviestreaming.view.BannerView;
import com.moviestreaming.view.ConsoleTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Controller handling Administrator management menus and administrative operations.
 */
public class AdminController {

    private final MovieService movieService;
    private final CategoryService categoryService;
    private final AnalyticsReportService analyticsReportService;
    private final MovieRankingEngine rankingEngine;
    private final MovieController movieController;
    private final UserSession userSession;

    public AdminController(MovieService movieService, CategoryService categoryService,
            MovieController movieController, UserSession userSession) {
        this(movieService, categoryService, null, null, movieController, userSession);
    }

    public AdminController(MovieService movieService, CategoryService categoryService,
            AnalyticsReportService analyticsReportService, MovieRankingEngine rankingEngine,
            MovieController movieController, UserSession userSession) {
        if (movieService == null || categoryService == null || movieController == null
                || userSession == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.movieService = movieService;
        this.categoryService = categoryService;
        this.analyticsReportService = analyticsReportService;
        this.rankingEngine = rankingEngine;
        this.movieController = movieController;
        this.userSession = userSession;
    }

    /**
     * Executes the Administrator Dashboard interactive loop.
     *
     * @param scanner console scanner
     */
    public void runAdminMenu(Scanner scanner) {
        if (!userSession.isAdmin()) {
            BannerView.printError("Access Denied: Administrator role required.");
            return;
        }

        boolean running = true;
        while (running) {
            BannerView.printSectionHeader("Administrator Dashboard");
            BannerView.printUserBadge(userSession.getCurrentUser().orElse(null));
            System.out.println("1. Movie Management");
            System.out.println("2. Category / Genre Management");
            System.out.println("3. System Statistics & Analytics");
            System.out.println("0. Return to Main Menu");

            int choice = InputHelper.readInt(scanner, "Select an option [0-3]: ", 0, 3);
            switch (choice) {
                case 1:
                    handleMovieManagement(scanner);
                    break;
                case 2:
                    handleCategoryManagement(scanner);
                    break;
                case 3:
                    handleSystemStatistics(scanner);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void handleMovieManagement(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            BannerView.printSectionHeader("Movie Management");
            System.out.println("1. List All Movies");
            System.out.println("2. View Movie Details");
            System.out.println("3. Add New Movie");
            System.out.println("4. Update Movie");
            System.out.println("5. Delete Movie");
            System.out.println("0. Back");

            int choice = InputHelper.readInt(scanner, "Select an option [0-5]: ", 0, 5);
            switch (choice) {
                case 1:
                    movieController.displayMovieTable(movieService.getAllMovies());
                    break;
                case 2:
                    String id = InputHelper.readNonEmptyString(scanner,
                            "Enter Movie ID (e.g. MOV-001): ");
                    movieController.displayMovieDetail(id);
                    break;
                case 3:
                    handleAddMovie(scanner);
                    break;
                case 4:
                    handleUpdateMovie(scanner);
                    break;
                case 5:
                    handleDeleteMovie(scanner);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void handleAddMovie(Scanner scanner) {
        BannerView.printSectionHeader("Add New Movie");
        displayCategoriesSummary();

        String title = InputHelper.readNonEmptyString(scanner, "Movie Title: ");
        String catId = InputHelper.readNonEmptyString(scanner, "Category ID (e.g. CAT-01): ");
        String director = InputHelper.readNonEmptyString(scanner, "Director: ");
        String actorsStr = InputHelper.readNonEmptyString(scanner, "Actors (comma-separated): ");
        int year = InputHelper.readInt(scanner, "Release Year [1888-2030]: ", 1888, 2030);
        int duration = InputHelper.readInt(scanner, "Duration in Minutes [1-600]: ", 1, 600);
        double rating = InputHelper.readDouble(scanner, "Rating [0.0-10.0]: ", 0.0, 10.0);
        String synopsis = InputHelper.readNonEmptyString(scanner, "Synopsis: ");

        List<String> actors = new ArrayList<>();
        for (String actor : actorsStr.split(",")) {
            if (!actor.trim().isEmpty()) {
                actors.add(actor.trim());
            }
        }

        try {
            Movie movie = new Movie(null, title, catId, director, actors, year, duration, rating,
                    0L, 0L, synopsis);
            Movie created = movieService.createMovie(movie);
            BannerView.printSuccess("Movie created successfully with ID: " + created.getId());
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void handleUpdateMovie(Scanner scanner) {
        BannerView.printSectionHeader("Update Movie");
        String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to update: ");
        Optional<Movie> movieOpt = movieService.getMovieById(id);
        if (!movieOpt.isPresent()) {
            BannerView.printError("Movie not found with ID: " + id);
            return;
        }

        Movie existing = movieOpt.get();
        movieController.displayMovieDetail(id);

        System.out.println("Press Enter to keep existing value in brackets [value].");

        System.out.print("New Title [" + existing.getTitle() + "]: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            title = existing.getTitle();
        }

        displayCategoriesSummary();
        System.out.print("New Category ID [" + existing.getCategoryId() + "]: ");
        String catId = scanner.nextLine().trim();
        if (catId.isEmpty()) {
            catId = existing.getCategoryId();
        }

        System.out.print("New Director [" + existing.getDirector() + "]: ");
        String director = scanner.nextLine().trim();
        if (director.isEmpty()) {
            director = existing.getDirector();
        }

        System.out.print("New Actors [" + String.join(", ", existing.getActors()) + "]: ");
        String actorsStr = scanner.nextLine().trim();
        List<String> actors = new ArrayList<>();
        if (!actorsStr.isEmpty()) {
            for (String a : actorsStr.split(",")) {
                if (!a.trim().isEmpty()) {
                    actors.add(a.trim());
                }
            }
        } else {
            actors = existing.getActors();
        }

        System.out.print("New Release Year [" + existing.getReleaseYear() + "]: ");
        String yearStr = scanner.nextLine().trim();
        int year = yearStr.isEmpty() ? existing.getReleaseYear() : Integer.parseInt(yearStr);

        System.out.print("New Duration [" + existing.getDurationMinutes() + "]: ");
        String durStr = scanner.nextLine().trim();
        int duration = durStr.isEmpty() ? existing.getDurationMinutes() : Integer.parseInt(durStr);

        System.out.print("New Rating [" + existing.getRating() + "]: ");
        String ratStr = scanner.nextLine().trim();
        double rating = ratStr.isEmpty() ? existing.getRating() : Double.parseDouble(ratStr);

        System.out.print("New Synopsis [" + existing.getSynopsis() + "]: ");
        String syn = scanner.nextLine().trim();
        if (syn.isEmpty()) {
            syn = existing.getSynopsis();
        }

        try {
            Movie updated = new Movie(id, title, catId, director, actors, year, duration, rating,
                    existing.getViewCount(), existing.getFavoriteCount(), syn);
            movieService.updateMovie(updated);
            BannerView.printSuccess("Movie updated successfully!");
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void handleDeleteMovie(Scanner scanner) {
        BannerView.printSectionHeader("Delete Movie");
        String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to delete: ");
        System.out.print("Are you sure you want to delete movie " + id + "? (y/N): ");
        String confirm = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            try {
                movieService.deleteMovie(id);
                BannerView.printSuccess("Movie deleted successfully.");
            } catch (AppException e) {
                BannerView.printError(e.getMessage());
            }
        } else {
            BannerView.printInfo("Deletion cancelled.");
        }
    }

    private void handleCategoryManagement(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            BannerView.printSectionHeader("Category / Genre Management");
            System.out.println("1. List All Categories");
            System.out.println("2. Add New Category");
            System.out.println("3. Update Category");
            System.out.println("4. Delete Category");
            System.out.println("0. Back");

            int choice = InputHelper.readInt(scanner, "Select an option [0-4]: ", 0, 4);
            switch (choice) {
                case 1:
                    displayCategoriesTable();
                    break;
                case 2:
                    String name = InputHelper.readNonEmptyString(scanner, "Category Name: ");
                    String desc = InputHelper.readNonEmptyString(scanner, "Description: ");
                    try {
                        Category created = categoryService.createCategory(name, desc);
                        BannerView.printSuccess("Category created with ID: " + created.getId());
                    } catch (AppException e) {
                        BannerView.printError(e.getMessage());
                    }
                    break;
                case 3:
                    String catId = InputHelper.readNonEmptyString(scanner,
                            "Enter Category ID to update: ");
                    String newName = InputHelper.readNonEmptyString(scanner, "New Category Name: ");
                    String newDesc = InputHelper.readNonEmptyString(scanner, "New Description: ");
                    try {
                        categoryService.updateCategory(catId, newName, newDesc);
                        BannerView.printSuccess("Category updated successfully.");
                    } catch (AppException e) {
                        BannerView.printError(e.getMessage());
                    }
                    break;
                case 4:
                    String delId = InputHelper.readNonEmptyString(scanner,
                            "Enter Category ID to delete: ");
                    try {
                        categoryService.deleteCategory(delId);
                        BannerView.printSuccess("Category deleted successfully.");
                    } catch (AppException e) {
                        BannerView.printError(e.getMessage());
                    }
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void handleSystemStatistics(Scanner scanner) {
        BannerView.printSectionHeader("Platform Statistics & Analytics");
        List<Movie> allMovies = movieService.getAllMovies();
        List<Category> allCategories = categoryService.getAllCategories();

        long totalViews = allMovies.stream().mapToLong(Movie::getViewCount).sum();
        long totalFavorites = allMovies.stream().mapToLong(Movie::getFavoriteCount).sum();
        double avgRating = allMovies.stream().mapToDouble(Movie::getRating).average().orElse(0.0);

        ConsoleTable table = new ConsoleTable("Metric", "Value");
        table.addRow("Total Movies in Catalog", String.valueOf(allMovies.size()));
        table.addRow("Total Movie Genres", String.valueOf(allCategories.size()));
        table.addRow("Total Platform Views", String.valueOf(totalViews));
        table.addRow("Total Platform Favorites", String.valueOf(totalFavorites));
        table.addRow("Average Catalog Rating", String.format("%.2f / 10.0 ⭐", avgRating));

        table.print();

        if (analyticsReportService != null) {
            System.out.println("\n--- Advanced Analytics Submenu ---");
            System.out.println("1. Trending Categories (Last 7 Days)");
            System.out.println("2. Trending Categories (Last 30 Days)");
            System.out.println("3. Top Ranked Movies (Weighted Max-Heap)");
            System.out.println("0. Back");

            int choice = InputHelper.readInt(scanner, "Option [0-3]: ", 0, 3);
            if (choice == 1) {
                displayTrending(7);
            } else if (choice == 2) {
                displayTrending(30);
            } else if (choice == 3 && rankingEngine != null) {
                displayTopRanked();
            }
        }
    }

    private void displayTrending(int days) {
        BannerView.printSectionHeader("Trending Categories in Last " + days + " Days");
        List<AnalyticsReportService.TrendingCategory> trending =
                analyticsReportService.getTrendingCategories(days, 5);
        if (trending.isEmpty()) {
            BannerView.printInfo("No viewing activity recorded in the last " + days + " days.");
            return;
        }
        ConsoleTable table =
                new ConsoleTable("Rank", "Category ID", "Category Name", "Watch Count");
        int rank = 1;
        for (AnalyticsReportService.TrendingCategory tc : trending) {
            table.addRow("#" + rank++, tc.getCategory().getId(), tc.getCategory().getName(),
                    String.valueOf(tc.getWatchCount()));
        }
        table.print();
    }

    private void displayTopRanked() {
        BannerView.printSectionHeader("Top Ranked Movies (Global Leaderboard)");
        List<MovieRankingEngine.RankedMovie> ranked = rankingEngine.rankMovies(10);
        ConsoleTable table =
                new ConsoleTable("Rank", "ID", "Title", "Score", "Rating", "Views", "Favorites");
        int rank = 1;
        for (MovieRankingEngine.RankedMovie rm : ranked) {
            Movie m = rm.getMovie();
            table.addRow("#" + rank++, m.getId(), m.getTitle(),
                    String.format("%.3f", rm.getScore()), m.getRating() + " ⭐",
                    String.valueOf(m.getViewCount()), String.valueOf(m.getFavoriteCount()));
        }
        table.print();
    }

    private void displayCategoriesSummary() {
        System.out.println("Available Categories: ");
        for (Category c : categoryService.getAllCategories()) {
            System.out.print("[" + c.getId() + ": " + c.getName() + "] ");
        }
        System.out.println();
    }

    private void displayCategoriesTable() {
        List<Category> categories = categoryService.getAllCategories();
        ConsoleTable table = new ConsoleTable("ID", "Name", "Description", "Movies Linked");
        for (Category c : categories) {
            int movieCount = movieService.getMoviesByCategory(c.getId()).size();
            table.addRow(c.getId(), c.getName(), c.getDescription(), String.valueOf(movieCount));
        }
        table.print();
    }
}
