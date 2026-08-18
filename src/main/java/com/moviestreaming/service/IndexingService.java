package com.moviestreaming.service;

import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * In-Memory Inverted Indexing Engine providing O(1)/O(K) lookups across movie attributes.
 * Maintains inverted indexes for Title, Actors, Director, Genre/Category, and Universal keyword search.
 */
public class IndexingService {

    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[\\s,\\.\\-_/\\\\:;!\'\"\\(\\)\\[\\]]+");

    private final Map<String, Set<String>> titleIndex = new HashMap<>();
    private final Map<String, Set<String>> actorIndex = new HashMap<>();
    private final Map<String, Set<String>> directorIndex = new HashMap<>();
    private final Map<String, Set<String>> categoryIdIndex = new HashMap<>();
    private final Map<String, Set<String>> genreNameIndex = new HashMap<>();
    private final Map<String, Set<String>> universalIndex = new HashMap<>();

    /**
     * Initializes and rebuilds the full in-memory index from collections of movies and categories.
     *
     * @param movies     list of movies to index
     * @param categories list of categories to resolve genre names
     */
    public synchronized void initialize(List<Movie> movies, List<Category> categories) {
        clearAllIndexes();

        Map<String, Category> categoryMap = new HashMap<>();
        if (categories != null) {
            for (Category c : categories) {
                if (c != null && c.getId() != null) {
                    categoryMap.put(c.getId(), c);
                }
            }
        }

        if (movies != null) {
            for (Movie movie : movies) {
                if (movie != null) {
                    Category cat = (movie.getCategoryId() != null) ? categoryMap.get(movie.getCategoryId()) : null;
                    indexMovie(movie, cat);
                }
            }
        }
    }

    /**
     * Incrementally indexes a movie entry.
     *
     * @param movie    the movie to index
     * @param category the category associated with this movie (optional)
     */
    public synchronized void indexMovie(Movie movie, Category category) {
        if (movie == null || movie.getId() == null) {
            return;
        }

        String movieId = movie.getId();
        evictMovie(movieId);

        // 1. Index Title tokens
        if (movie.getTitle() != null) {
            Set<String> titleTokens = extractTokens(movie.getTitle());
            for (String token : titleTokens) {
                addToIndex(titleIndex, token, movieId);
                addToIndex(universalIndex, token, movieId);
            }
            addToIndex(titleIndex, normalizeExact(movie.getTitle()), movieId);
        }

        // 2. Index Actor names and tokens
        if (movie.getActors() != null) {
            for (String actor : movie.getActors()) {
                if (actor != null && !actor.trim().isEmpty()) {
                    String normActor = normalizeExact(actor);
                    addToIndex(actorIndex, normActor, movieId);
                    addToIndex(universalIndex, normActor, movieId);

                    for (String token : extractTokens(actor)) {
                        addToIndex(actorIndex, token, movieId);
                        addToIndex(universalIndex, token, movieId);
                    }
                }
            }
        }

        // 3. Index Director name and tokens
        if (movie.getDirector() != null && !movie.getDirector().trim().isEmpty()) {
            String normDirector = normalizeExact(movie.getDirector());
            addToIndex(directorIndex, normDirector, movieId);
            addToIndex(universalIndex, normDirector, movieId);

            for (String token : extractTokens(movie.getDirector())) {
                addToIndex(directorIndex, token, movieId);
                addToIndex(universalIndex, token, movieId);
            }
        }

        // 4. Index Category ID
        if (movie.getCategoryId() != null && !movie.getCategoryId().trim().isEmpty()) {
            String normCatId = movie.getCategoryId().trim().toUpperCase(Locale.ROOT);
            addToIndex(categoryIdIndex, normCatId, movieId);
            addToIndex(universalIndex, normCatId.toLowerCase(Locale.ROOT), movieId);
        }

        // 5. Index Genre Name tokens
        if (category != null && category.getName() != null) {
            String normGenre = normalizeExact(category.getName());
            addToIndex(genreNameIndex, normGenre, movieId);
            addToIndex(universalIndex, normGenre, movieId);

            for (String token : extractTokens(category.getName())) {
                addToIndex(genreNameIndex, token, movieId);
                addToIndex(universalIndex, token, movieId);
            }
        }

        // 6. Index Synopsis tokens into universal search
        if (movie.getSynopsis() != null) {
            for (String token : extractTokens(movie.getSynopsis())) {
                addToIndex(universalIndex, token, movieId);
            }
        }
    }

    /**
     * Evicts a movie ID from all inverted indexes.
     *
     * @param movieId the movie ID to remove
     */
    public synchronized void evictMovie(String movieId) {
        if (movieId == null) {
            return;
        }
        removeFromIndexMap(titleIndex, movieId);
        removeFromIndexMap(actorIndex, movieId);
        removeFromIndexMap(directorIndex, movieId);
        removeFromIndexMap(categoryIdIndex, movieId);
        removeFromIndexMap(genreNameIndex, movieId);
        removeFromIndexMap(universalIndex, movieId);
    }

    /**
     * Looks up movie IDs matching title search query.
     *
     * @param query the search query
     * @return set of matching movie IDs
     */
    public synchronized Set<String> searchMovieIdsByTitle(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return matchTokens(titleIndex, query);
    }

    /**
     * Looks up movie IDs matching actor search query.
     *
     * @param query the actor name or token
     * @return set of matching movie IDs
     */
    public synchronized Set<String> searchMovieIdsByActor(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return matchTokens(actorIndex, query);
    }

    /**
     * Looks up movie IDs matching director search query.
     *
     * @param query the director name or token
     * @return set of matching movie IDs
     */
    public synchronized Set<String> searchMovieIdsByDirector(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return matchTokens(directorIndex, query);
    }

    /**
     * Looks up movie IDs matching genre name or category ID.
     *
     * @param query the genre name or category ID
     * @return set of matching movie IDs
     */
    public synchronized Set<String> searchMovieIdsByGenre(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }
        String trimmed = query.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);

        // Check if query is category ID
        if (categoryIdIndex.containsKey(upper)) {
            return new HashSet<>(categoryIdIndex.get(upper));
        }

        return matchTokens(genreNameIndex, trimmed);
    }

    /**
     * Looks up movie IDs across all fields with universal keyword search.
     *
     * @param keyword the search keyword
     * @return set of matching movie IDs
     */
    public synchronized Set<String> searchMovieIdsUniversal(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return matchTokens(universalIndex, keyword);
    }

    private Set<String> matchTokens(Map<String, Set<String>> indexMap, String query) {
        String exact = normalizeExact(query);
        if (indexMap.containsKey(exact)) {
            return new HashSet<>(indexMap.get(exact));
        }

        Set<String> queryTokens = extractTokens(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> resultSet = null;
        for (String token : queryTokens) {
            Set<String> tokenMatches = indexMap.getOrDefault(token, Collections.emptySet());
            if (resultSet == null) {
                resultSet = new HashSet<>(tokenMatches);
            } else {
                // Conjunction: match movies containing all query tokens
                resultSet.retainAll(tokenMatches);
            }
            if (resultSet.isEmpty()) {
                break;
            }
        }

        return (resultSet != null) ? resultSet : Collections.emptySet();
    }

    private void addToIndex(Map<String, Set<String>> indexMap, String key, String movieId) {
        if (key == null || key.isEmpty() || movieId == null) {
            return;
        }
        indexMap.computeIfAbsent(key, k -> new HashSet<>()).add(movieId);
    }

    private void removeFromIndexMap(Map<String, Set<String>> indexMap, String movieId) {
        indexMap.values().forEach(set -> set.remove(movieId));
    }

    private void clearAllIndexes() {
        titleIndex.clear();
        actorIndex.clear();
        directorIndex.clear();
        categoryIdIndex.clear();
        genreNameIndex.clear();
        universalIndex.clear();
    }

    private String normalizeExact(String input) {
        return input.trim().toLowerCase(Locale.ROOT);
    }

    private Set<String> extractTokens(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptySet();
        }
        String[] rawTokens = WORD_SPLIT_PATTERN.split(input.toLowerCase(Locale.ROOT));
        return Arrays.stream(rawTokens)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
    }
}
