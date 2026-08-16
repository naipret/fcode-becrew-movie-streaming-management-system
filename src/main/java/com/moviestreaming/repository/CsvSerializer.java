package com.moviestreaming.repository;

/**
 * Strategy interface defining how a specific domain entity is converted to
 * and from CSV lines, along with identifying its primary key.
 *
 * @param <T>  the domain entity type
 * @param <ID> the entity identifier type
 */
public interface CsvSerializer<T, ID> {

    /**
     * Returns the CSV header row (e.g. "id|title|releaseYear").
     *
     * @return the header line
     */
    String getHeader();

    /**
     * Serializes an entity into a formatted CSV line.
     *
     * @param entity the entity to serialize
     * @return the CSV line
     */
    String serialize(T entity);

    /**
     * Deserializes a single CSV line into an entity instance.
     *
     * @param csvLine the raw CSV line
     * @return the parsed entity instance
     */
    T deserialize(String csvLine);

    /**
     * Extracts the primary key ID from an entity instance.
     *
     * @param entity the entity instance
     * @return the entity ID
     */
    ID extractId(T entity);
}
