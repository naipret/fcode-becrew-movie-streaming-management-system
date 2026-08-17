package com.moviestreaming.repository;

import com.moviestreaming.model.User;
import java.util.Optional;

/**
 * Concrete file-backed repository for User accounts.
 */
public class UserRepository extends GenericFileRepository<User, String> {

    public UserRepository(String filePath) {
        super(filePath, new UserSerializer());
    }

    /**
     * Finds a user account by case-insensitive username.
     *
     * @param username the username to find
     * @return Optional containing User if found, empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        synchronized (cache) {
            return cache.values().stream()
                    .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()))
                    .findFirst();
        }
    }

    /**
     * Finds a user account by case-insensitive email address.
     *
     * @param email the email address to find
     * @return Optional containing User if found, empty otherwise
     */
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        synchronized (cache) {
            return cache.values().stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim()))
                    .findFirst();
        }
    }
}
