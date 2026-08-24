package com.moviestreaming.service;

import com.moviestreaming.exception.AuthenticationException;
import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.User;
import com.moviestreaming.model.UserRole;
import com.moviestreaming.repository.UserRepository;
import com.moviestreaming.validator.UserValidator;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service managing user authentication, registration, password verification, and session lifecycle.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final UserSession userSession;
    private final UserValidator validator;

    public AuthService(UserRepository userRepository, UserSession userSession) {
        if (userRepository == null || userSession == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        this.userRepository = userRepository;
        this.userSession = userSession;
        this.validator = new UserValidator();
    }

    /**
     * Authenticates a user with username and password, setting the active UserSession.
     *
     * @param username the username
     * @param password the password
     * @return the authenticated User
     * @throws AuthenticationException if credentials are invalid
     */
    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Username and password must not be empty.");
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        userSession.setCurrentUser(user);
        return user;
    }

    /**
     * Registers a new user account with default role USER.
     *
     * @param username the desired username
     * @param password the password (min 6 characters)
     * @param fullName user's full name
     * @param email    user's email address
     * @return the created User
     */
    public User register(String username, String password, String fullName, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty.");
        }

        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim();

        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            throw new DuplicateEntityException("User", "username", trimmedUsername);
        }

        if (userRepository.findByEmail(trimmedEmail).isPresent()) {
            throw new DuplicateEntityException("User", "email", trimmedEmail);
        }

        String nextId = generateNextId();
        User newUser = new User(
                nextId,
                trimmedUsername,
                password,
                fullName != null ? fullName.trim() : "",
                trimmedEmail,
                UserRole.USER,
                LocalDateTime.now()
        );

        validator.validate(newUser);
        return userRepository.save(newUser);
    }

    /**
     * Logs out the current user by clearing the session.
     */
    public void logout() {
        userSession.clear();
    }

    /**
     * Returns the currently authenticated user if present.
     *
     * @return Optional containing the current User
     */
    public Optional<User> getCurrentUser() {
        return userSession.getCurrentUser();
    }

    /**
     * Returns the active UserSession.
     *
     * @return the UserSession
     */
    public UserSession getUserSession() {
        return userSession;
    }

    private synchronized String generateNextId() {
        int maxIndex = userRepository.findAll().stream()
                .map(User::getId)
                .filter(id -> id != null && id.startsWith("USR-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(4));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("USR-%03d", maxIndex + 1);
    }
}
