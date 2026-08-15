package com.moviestreaming.exception;

/**
 * Exception thrown when attempting to create or update an entity with a duplicate unique field.
 */
public class DuplicateEntityException extends AppException {

    private static final long serialVersionUID = 1L;

    public DuplicateEntityException(String entityName, String fieldName, Object value) {
        super(String.format("%s with %s '%s' already exists.", entityName, fieldName, value));
    }

    public DuplicateEntityException(String message) {
        super(message);
    }
}
