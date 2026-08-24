package com.moviestreaming.service;

import com.moviestreaming.model.User;
import com.moviestreaming.model.UserRole;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Session container holding the state of the currently authenticated user.
 */
public class UserSession {

    private User currentUser;
    private LocalDateTime loginTimestamp;

    public UserSession() {
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
        this.loginTimestamp = (user != null) ? LocalDateTime.now() : null;
    }

    public synchronized Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    public synchronized LocalDateTime getLoginTimestamp() {
        return loginTimestamp;
    }

    public synchronized void clear() {
        this.currentUser = null;
        this.loginTimestamp = null;
    }
}
