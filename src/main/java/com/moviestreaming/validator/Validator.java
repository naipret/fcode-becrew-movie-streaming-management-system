package com.moviestreaming.validator;

import com.moviestreaming.exception.ValidationException;

/**
 * Generic contract for domain entity validators.
 *
 * @param <T> the entity type
 */
public interface Validator<T> {

    /**
     * Validates the domain entity and throws ValidationException if constraints are violated.
     *
     * @param entity the entity to validate
     * @throws ValidationException if validation fails
     */
    void validate(T entity) throws ValidationException;
}
