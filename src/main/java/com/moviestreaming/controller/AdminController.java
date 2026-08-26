package com.moviestreaming.controller;

import com.moviestreaming.exception.AppException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.service.CategoryService;
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
    private final MovieController movieController;
    private final UserSession userSession;

    public AdminController(MovieService movieService, CategoryService categoryService,
                           MovieController movieController, UserSession userSession) {
        if (movieService == null || categoryService == null || movieController == null || userSession == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.movieService = movieService;
        this.categoryService = categoryService;
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
                    handleSystemStatistics();
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
                    String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID (e.g. MOV-001): ");
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
        String title = InputHelper.readNonEmptyString(scanner, "Title: ");
        displayCategoriesSummary();
        String catId = InputHelper.readNonEmptyString(scanner, "Category ID (e.g. CAT-01): ");
        String director = InputHelper.readNonEmptyString(scanner, "Director: ");
        String actorsStr = InputHelper.readNonEmptyString(scanner, "Actors (comma-separated): ");
        List<String> actors = new ArrayList<>();
        for (String a : actorsStr.split(",")) {
            if (!a.trim().isEmpty()) {
                actors.add(a.trim());
            }
        }
        int year = InputHelper.readInt(scanner, "Release Year (1888-2035): ", 1888, 2035);
        int duration = InputHelper.readInt(scanner, "Duration (minutes): ", 1, 1000);
        double rating = InputHelper.readDouble(scanner, "Rating (0.0 - 10.0): ", 0.0, 10.0);
        String synopsis = InputHelper.readNonEmptyString(scanner, "Synopsis: ");

        Movie movie = new Movie(null, title, catId, director, actors, year, duration, rating, 0, 0, synopsis);
        try {
            Movie created = movieService.createMovie(movie);
            BannerView.printSuccess("Movie successfully created with ID: " + created.getId());
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void handleUpdateMovie(Scanner scanner) {
        BannerView.printSectionHeader("Update Movie");
        String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to update: ");
        Optional<Movie> existingOpt = movieService.getMovieById(id);
        if (!existingOpt.isPresent()) {
            BannerView.printError("Movie not found.");
            return;
        }

        Movie movie = existingOpt.get();
        movieController.displayMovieDetail(id);

        String title = InputHelper.readNonEmptyString(scanner, "New Title [" + movie.getTitle() + "]: ");
        displayCategoriesSummary();
        String catId = InputHelper.readNonEmptyString(scanner, "New Category ID [" + movie.getCategoryId() + "]: ");
        String director = InputHelper.readNonEmptyString(scanner, "New Director [" + movie.getDirector() + "]: ");
        int year = InputHelper.readInt(scanner, "New Release Year [" + movie.getReleaseYear() + "]: ", 1888, 2035);
        int duration = InputHelper.readInt(scanner, "New Duration [" + movie.getDurationMinutes() + "]: ", 1, 1000);
        double rating = InputHelper.readDouble(scanner, "New Rating [" + movie.getRating() + "]: ", 0.0, 10.0);

        movie.setTitle(title);
        movie.setCategoryId(catId);
        movie.setDirector(director);
        movie.setReleaseYear(year);
        movie.setDurationMinutes(duration);
        movie.setRating(rating);

        try {
            movieService.updateMovie(movie);
            BannerView.printSuccess("Movie updated successfully!");
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void handleDeleteMovie(Scanner scanner) {
        BannerView.printSectionHeader("Delete Movie");
        String id = InputHelper.readNonEmptyString(scanner, "Enter Movie ID to delete: ");
        try {
            movieService.deleteMovie(id);
            BannerView.printSuccess("Movie " + id + " deleted successfully.");
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
        }
    }

    private void handleCategoryManagement(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            BannerView.printSectionHeader("Category / Genre Management");
            System.out.println("1. List All Categories");
            System.out.println("2. Add New Category");
            System.out.println("3. Update Category");
            System.out.println("4. Delete Category (Checks Referential Integrity)");
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
                    String catId = InputHelper.readNonEmptyString(scanner, "Enter Category ID to update: ");
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
                    String delId = InputHelper.readNonEmptyString(scanner, "Enter Category ID to delete: ");
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

    private void handleSystemStatistics() {
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
