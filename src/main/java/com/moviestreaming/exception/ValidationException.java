package com.moviestreaming.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when domain validation or console input validation fails.
 */
public class ValidationException extends AppException {

    private static final long serialVersionUID = 1L;

    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Collections.singletonList(message);
    }

    public ValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = new ArrayList<>(errors);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
