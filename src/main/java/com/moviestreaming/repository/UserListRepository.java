package com.moviestreaming.repository;

import com.moviestreaming.exception.StorageException;
import com.moviestreaming.util.AtomicFileWriter;
import com.moviestreaming.util.CsvSanitizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository managing user-to-movie list relationships (such as Watchlists and Favorites).
 * Provides thread-safe in-memory caching and safe atomic persistence.
 */
public class UserListRepository {

    private static final Logger LOGGER = Logger.getLogger(UserListRepository.class.getName());
    private static final String CSV_HEADER = "userId|movieId";

    private final String filePath;
    private final Map<String, Set<String>> userToMoviesMap = new ConcurrentHashMap<>();

    public UserListRepository(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }
        this.filePath = filePath;
        loadFromFile();
    }

    /**
     * Adds a movie to a user's list and saves to file.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     * @return true if added, false if already present
     */
    public synchronized boolean add(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return false;
        }
        Set<String> set = userToMoviesMap.computeIfAbsent(userId, k -> new LinkedHashSet<>());
        boolean added = set.add(movieId);
        if (added) {
            saveToFile();
        }
        return added;
    }

    /**
     * Removes a movie from a user's list and saves to file.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     * @return true if removed, false if not found
     */
    public synchronized boolean remove(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return false;
        }
        Set<String> set = userToMoviesMap.get(userId);
        if (set != null && set.remove(movieId)) {
            if (set.isEmpty()) {
                userToMoviesMap.remove(userId);
            }
            saveToFile();
            return true;
        }
        return false;
    }

    /**
     * Retrieves the set of movie IDs for a user.
     *
     * @param userId the user ID
     * @return unmodifiable set of movie IDs
     */
    public Set<String> getMovieIds(String userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        Set<String> set = userToMoviesMap.get(userId);
        return set != null ? Collections.unmodifiableSet(new LinkedHashSet<>(set)) : Collections.emptySet();
    }

    /**
     * Checks whether a movie is in a user's list.
     *
     * @param userId  the user ID
     * @param movieId the movie ID
     * @return true if present
     */
    public boolean contains(String userId, String movieId) {
        if (userId == null || movieId == null) {
            return false;
        }
        Set<String> set = userToMoviesMap.get(userId);
        return set != null && set.contains(movieId);
    }

    /**
     * Returns total count of associations.
     *
     * @return total associations
     */
    public int count() {
        return userToMoviesMap.values().stream().mapToInt(Set::size).sum();
    }

    private synchronized void loadFromFile() {
        userToMoviesMap.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // skip header
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> tokens = CsvSanitizer.split(line);
                if (tokens.size() < 2) {
                    LOGGER.log(Level.WARNING, "Skipped invalid list entry line {0} in {1}: {2}",
                            new Object[]{lineNumber, file.getName(), line});
                    continue;
                }
                String userId = tokens.get(0).trim();
                String movieId = tokens.get(1).trim();
                userToMoviesMap.computeIfAbsent(userId, k -> new LinkedHashSet<>()).add(movieId);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read user movie list file: " + filePath, e);
        }
    }

    private synchronized void saveToFile() {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);

        for (Map.Entry<String, Set<String>> entry : userToMoviesMap.entrySet()) {
            String userId = entry.getKey();
            for (String movieId : entry.getValue()) {
                lines.add(userId + "|" + movieId);
            }
        }

        AtomicFileWriter.writeLines(filePath, lines);
    }
}
