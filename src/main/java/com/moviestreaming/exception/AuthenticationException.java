package com.moviestreaming.exception;

/**
 * Exception thrown when user login, registration, or session validation fails.
 */
public class AuthenticationException extends AppException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
