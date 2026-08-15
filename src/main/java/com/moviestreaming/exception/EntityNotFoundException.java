package com.moviestreaming.exception;

/**
 * Exception thrown when a requested entity (Movie, Category, User, etc.) cannot be found.
 */
public class EntityNotFoundException extends AppException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s with ID '%s' was not found.", entityName, id));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
