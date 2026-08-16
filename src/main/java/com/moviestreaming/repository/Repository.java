package com.moviestreaming.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD Repository interface for data access.
 *
 * @param <T>  the entity type
 * @param <ID> the primary key identifier type
 */
public interface Repository<T, ID> {

    /**
     * Retrieves all managed entities.
     *
     * @return list of all entities
     */
    List<T> findAll();

    /**
     * Finds an entity by its unique ID.
     *
     * @param id the entity identifier
     * @return Optional containing entity if found, empty otherwise
     */
    Optional<T> findById(ID id);

    /**
     * Saves or updates an entity in memory and persists to storage.
     *
     * @param entity the entity to save
     * @return the saved entity instance
     */
    T save(T entity);

    /**
     * Deletes an entity by its unique ID.
     *
     * @param id the entity identifier
     */
    void deleteById(ID id);

    /**
     * Checks if an entity exists by its unique ID.
     *
     * @param id the entity identifier
     * @return true if entity exists, false otherwise
     */
    boolean existsById(ID id);

    /**
     * Returns the total count of managed entities.
     *
     * @return count of entities
     */
    long count();

    /**
     * Clears all entities from cache and storage.
     */
    void clear();
}
