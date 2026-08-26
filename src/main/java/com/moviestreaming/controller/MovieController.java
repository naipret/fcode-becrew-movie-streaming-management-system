package com.moviestreaming.controller;

import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.service.CategoryService;
import com.moviestreaming.service.MovieService;
import com.moviestreaming.view.BannerView;
import com.moviestreaming.view.ConsoleTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Presentation controller providing formatted tables and cards for movie data.
 */
public class MovieController {

    private final MovieService movieService;
    private final CategoryService categoryService;

    public MovieController(MovieService movieService, CategoryService categoryService) {
        if (movieService == null || categoryService == null) {
            throw new IllegalArgumentException("Services must not be null");
        }
        this.movieService = movieService;
        this.categoryService = categoryService;
    }

    /**
     * Renders a list of movies as an ASCII table.
     *
     * @param movies list of movies to display
     */
    public void displayMovieTable(List<Movie> movies) {
        if (movies == null || movies.isEmpty()) {
            BannerView.printInfo("No movies found.");
            return;
        }

        // Cache category names
        Map<String, String> categoryNames = new HashMap<>();
        for (Category c : categoryService.getAllCategories()) {
            categoryNames.put(c.getId(), c.getName());
        }

        ConsoleTable table = new ConsoleTable("ID", "Title", "Genre", "Rating", "Year", "Duration", "Views", "Likes");
        table.setMaxColumnWidth(30);

        for (Movie m : movies) {
            String genre = categoryNames.getOrDefault(m.getCategoryId(), m.getCategoryId());
            table.addRow(
                    m.getId(),
                    m.getTitle(),
                    genre,
                    String.format("%.1f ⭐", m.getRating()),
                    String.valueOf(m.getReleaseYear()),
                    m.getDurationMinutes() + "m",
                    String.valueOf(m.getViewCount()),
                    String.valueOf(m.getFavoriteCount())
            );
        }

        table.print();
        System.out.println("Total: " + movies.size() + " movie(s)");
    }

    /**
     * Displays the rich card for a single movie by ID.
     *
     * @param movieId the movie ID
     */
    public void displayMovieDetail(String movieId) {
        if (movieId == null || movieId.trim().isEmpty()) {
            return;
        }
        Optional<Movie> movieOpt = movieService.getMovieById(movieId.trim());
        if (!movieOpt.isPresent()) {
            BannerView.printError("Movie with ID '" + movieId + "' not found.");
            return;
        }

        Movie movie = movieOpt.get();
        Category category = categoryService.getCategoryById(movie.getCategoryId()).orElse(null);
        BannerView.printMovieCard(movie, category);
    }
}
