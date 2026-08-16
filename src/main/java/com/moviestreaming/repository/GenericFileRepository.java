package com.moviestreaming.repository;

import com.moviestreaming.exception.StorageException;
import com.moviestreaming.util.AtomicFileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract Generic File Repository combining fast in-memory caching with
 * resilient, atomic CSV file persistence.
 *
 * @param <T>  the domain entity type
 * @param <ID> the primary key identifier type
 */
public abstract class GenericFileRepository<T, ID> implements Repository<T, ID> {

    protected final Path filePath;
    protected final CsvSerializer<T, ID> serializer;
    protected final Map<ID, T> cache = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Constructs a repository, sets the file path and serializer, and initializes data.
     *
     * @param filePath   the path string to the CSV file
     * @param serializer the CSV serializer for entity T
     */
    protected GenericFileRepository(String filePath, CsvSerializer<T, ID> serializer) {
        if (filePath == null || serializer == null) {
            throw new IllegalArgumentException("FilePath and Serializer must not be null");
        }
        this.filePath = Paths.get(filePath);
        this.serializer = serializer;
        loadFromFile();
    }

    /**
     * Loads all entities from the CSV file into the in-memory cache with resilient error handling.
     */
    public synchronized void loadFromFile() {
        cache.clear();
        if (!Files.exists(filePath)) {
            // If file does not exist, write header to initialize an empty file
            saveToFile();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                saveToFile();
                return;
            }

            // Skip header (line 0) and parse data rows
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                try {
                    T entity = serializer.deserialize(line);
                    if (entity != null) {
                        ID id = serializer.extractId(entity);
                        if (id != null) {
                            cache.put(id, entity);
                        }
                    }
                } catch (Exception e) {
                    // Resilient Ingestion: Log warning and continue processing remaining rows
                    System.err.println(String.format(
                            "[WARN] Resilient Parser: Skipped corrupted line %d in %s: %s (Reason: %s)",
                            i + 1, filePath.getFileName(), line, e.getMessage()
                    ));
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read data from " + filePath, e);
        }
    }

    /**
     * Persists the current in-memory cache to the CSV file using AtomicFileWriter.
     */
    public synchronized void saveToFile() {
        List<String> lines = new ArrayList<>();
        lines.add(serializer.getHeader());

        synchronized (cache) {
            for (T entity : cache.values()) {
                if (entity != null) {
                    lines.add(serializer.serialize(entity));
                }
            }
        }

        AtomicFileWriter.writeLines(filePath, lines, StandardCharsets.UTF_8);
    }

    @Override
    public synchronized List<T> findAll() {
        synchronized (cache) {
            return new ArrayList<>(cache.values());
        }
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public synchronized T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity to save cannot be null");
        }
        ID id = serializer.extractId(entity);
        if (id == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }

        cache.put(id, entity);
        saveToFile();
        return entity;
    }

    @Override
    public synchronized void deleteById(ID id) {
        if (id != null && cache.containsKey(id)) {
            cache.remove(id);
            saveToFile();
        }
    }

    @Override
    public synchronized boolean existsById(ID id) {
        if (id == null) {
            return false;
        }
        return cache.containsKey(id);
    }

    @Override
    public synchronized long count() {
        return cache.size();
    }

    @Override
    public synchronized void clear() {
        cache.clear();
        saveToFile();
    }
}
