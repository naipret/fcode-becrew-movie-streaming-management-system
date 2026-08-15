package com.moviestreaming.exception;

/**
 * Root unchecked exception for all application-level errors.
 */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
