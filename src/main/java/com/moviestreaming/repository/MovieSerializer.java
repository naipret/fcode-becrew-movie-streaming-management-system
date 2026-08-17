package com.moviestreaming.repository;

import com.moviestreaming.model.Movie;
import com.moviestreaming.util.CsvSanitizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CSV Serializer for Movie entity.
 */
public class MovieSerializer implements CsvSerializer<Movie, String> {

    @Override
    public String getHeader() {
        return "id|title|categoryId|director|actors|releaseYear|durationMinutes|rating|viewCount|favoriteCount|synopsis";
    }

    @Override
    public String serialize(Movie movie) {
        if (movie == null) {
            return "";
        }
        String actorsFormatted = (movie.getActors() != null && !movie.getActors().isEmpty())
                ? String.join(",", movie.getActors())
                : "";

        return CsvSanitizer.join(Arrays.asList(
                movie.getId(),
                movie.getTitle(),
                movie.getCategoryId(),
                movie.getDirector(),
                actorsFormatted,
                String.valueOf(movie.getReleaseYear()),
                String.valueOf(movie.getDurationMinutes()),
                String.valueOf(movie.getRating()),
                String.valueOf(movie.getViewCount()),
                String.valueOf(movie.getFavoriteCount()),
                movie.getSynopsis()
        ));
    }

    @Override
    public Movie deserialize(String csvLine) {
        List<String> tokens = CsvSanitizer.split(csvLine);
        if (tokens.size() < 11) {
            throw new IllegalArgumentException("Invalid Movie CSV format, expected 11 columns but got: " + tokens.size());
        }

        String id = tokens.get(0);
        String title = tokens.get(1);
        String categoryId = tokens.get(2);
        String director = tokens.get(3);
        String actorsStr = tokens.get(4);
        List<String> actors = new ArrayList<>();
        if (actorsStr != null && !actorsStr.trim().isEmpty()) {
            for (String a : actorsStr.split(",")) {
                if (!a.trim().isEmpty()) {
                    actors.add(a.trim());
                }
            }
        }
        int releaseYear = Integer.parseInt(tokens.get(5).trim());
        int durationMinutes = Integer.parseInt(tokens.get(6).trim());
        double rating = Double.parseDouble(tokens.get(7).trim());
        long viewCount = Long.parseLong(tokens.get(8).trim());
        long favoriteCount = Long.parseLong(tokens.get(9).trim());
        String synopsis = tokens.get(10);

        return new Movie(id, title, categoryId, director, actors, releaseYear,
                durationMinutes, rating, viewCount, favoriteCount, synopsis);
    }

    @Override
    public String extractId(Movie movie) {
        return movie != null ? movie.getId() : null;
    }
}
