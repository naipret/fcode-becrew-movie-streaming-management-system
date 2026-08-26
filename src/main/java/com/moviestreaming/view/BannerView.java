package com.moviestreaming.view;

import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.User;

/**
 * View presentation utility for ANSI ASCII Art Banners, Section Titles, and Movie Detail Cards.
 */
public final class BannerView {

    private BannerView() {
        // Utility class
    }

    /**
     * Prints the primary application ASCII header banner.
     */
    public static void printAppBanner() {
        System.out.println(AnsiColor.RED + "================================================================================");
        System.out.println("  _   _ ______ _______ ______ _      _______  __    _____ _      _____ ");
        System.out.println(" | \\ | |  ____|__   __|  ____| |    |_   _\\ \\/ /   / ____| |    |_   _|");
        System.out.println(" |  \\| | |__     | |  | |__  | |      | |  \\  /   | |    | |      | |  ");
        System.out.println(" | . ` |  __|    | |  |  __| | |      | |  /  \\   | |    | |      | |  ");
        System.out.println(" | |\\  | |____   | |  | |    | |____ _| |_/ /\\ \\  | |____| |____ _| |_ ");
        System.out.println(" |_| \\_|______|  |_|  |_|    |______|_____/_/  \\_\\  \\_____|______|_____|");
        System.out.println("                                                                        ");
        System.out.println("          NETFLIX STREAMING & MOVIE MANAGEMENT SYSTEM v1.0.0            ");
        System.out.println("================================================================================" + AnsiColor.RESET);
    }

    /**
     * Prints a stylized section header title.
     *
     * @param title section title
     */
    public static void printSectionHeader(String title) {
        System.out.println("\n" + AnsiColor.CYAN + ">>> " + AnsiColor.BOLD
                + title.toUpperCase() + AnsiColor.RESET + AnsiColor.CYAN + " <<<" + AnsiColor.RESET);
        System.out.println(AnsiColor.CYAN
                + "--------------------------------------------------------------------------------" + AnsiColor.RESET);
    }

    /**
     * Prints a success notice box.
     *
     * @param message success message
     */
    public static void printSuccess(String message) {
        System.out.println(AnsiColor.GREEN + "[SUCCESS] " + message + AnsiColor.RESET);
    }

    /**
     * Prints an error notice box.
     *
     * @param message error message
     */
    public static void printError(String message) {
        System.out.println(AnsiColor.RED + "[ERROR] " + message + AnsiColor.RESET);
    }

    /**
     * Prints an informational or warning notice.
     *
     * @param message info message
     */
    public static void printInfo(String message) {
        System.out.println(AnsiColor.YELLOW + "[INFO] " + message + AnsiColor.RESET);
    }

    /**
     * Prints the user profile status badge.
     *
     * @param user authenticated user
     */
    public static void printUserBadge(User user) {
        if (user != null) {
            String roleColor = user.getRole() != null && user.getRole().name().equals("ADMIN")
                    ? AnsiColor.PURPLE : AnsiColor.BLUE;
            System.out.println(AnsiColor.BOLD + "Logged in as: " + AnsiColor.RESET
                    + user.getFullName() + " (" + roleColor + user.getRole() + AnsiColor.RESET + " | @" + user.getUsername() + ")");
        }
    }

    /**
     * Renders a rich formatted card for a single movie's full metadata.
     *
     * @param movie    the movie entity
     * @param category the category entity (optional)
     */
    public static void printMovieCard(Movie movie, Category category) {
        if (movie == null) {
            return;
        }

        String genreName = (category != null) ? category.getName() : movie.getCategoryId();
        String actorsStr = (movie.getActors() != null && !movie.getActors().isEmpty())
                ? String.join(", ", movie.getActors()) : "N/A";

        System.out.println("\n" + AnsiColor.YELLOW + "+------------------------------------------------------------------------------+");
        System.out.println("| " + AnsiColor.BOLD + padRight(movie.getTitle() + " (" + movie.getReleaseYear() + ")", 76)
                + AnsiColor.YELLOW + " |");
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| ID: %-15s | Genre: %-22s | Rating: %-19s |%n",
                movie.getId(), genreName, movie.getRating() + " / 10.0 ⭐");
        System.out.printf("| Director: %-25s | Duration: %-28s |%n",
                movie.getDirector(), movie.getDurationMinutes() + " mins");
        System.out.printf("| Views: %-15s | Favorites: %-20s | Year: %-21s |%n",
                movie.getViewCount() + " 👁", movie.getFavoriteCount() + " ❤", movie.getReleaseYear());
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("| Cast: " + padRight(actorsStr, 70) + " |");
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("| Synopsis:                                                                    |");
        System.out.println("| " + padRight(movie.getSynopsis() != null ? movie.getSynopsis() : "No synopsis provided.", 76) + " |");
        System.out.println("+------------------------------------------------------------------------------+" + AnsiColor.RESET);
    }

    private static String padRight(String s, int n) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= n) {
            return (n > 3) ? s.substring(0, n - 3) + "..." : s.substring(0, n);
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
