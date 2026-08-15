package com.moviestreaming.exception;

/**
 * Exception thrown when file reading, writing, parsing, or atomic replacement fails.
 */
public class StorageException extends AppException {

    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
