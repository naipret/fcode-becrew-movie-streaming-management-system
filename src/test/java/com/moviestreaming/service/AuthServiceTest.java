package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moviestreaming.exception.AuthenticationException;
import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.model.User;
import com.moviestreaming.model.UserRole;
import com.moviestreaming.repository.UserRepository;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("AuthService & UserSession Test Suite")
class AuthServiceTest {

    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private UserSession userSession;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        Path userFile = tempDir.resolve("users.csv");
        userRepository = new UserRepository(userFile.toString());
        userSession = new UserSession();
        authService = new AuthService(userRepository, userSession);
    }

    @Test
    @DisplayName("Should register a new user account and reject duplicate username/email")
    void shouldRegisterUserAndRejectDuplicates() {
        User user = authService.register("john_doe", "secret123", "John Doe", "john@example.com");
        assertThat(user.getId()).isEqualTo("USR-001");
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);

        assertThatThrownBy(() -> authService.register("john_doe", "pass456", "Other", "other@example.com"))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("User with username 'john_doe' already exists");

        assertThatThrownBy(() -> authService.register("unique_user", "pass456", "Other", "john@example.com"))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("User with email 'john@example.com' already exists");
    }

    @Test
    @DisplayName("Should authenticate valid user, manage session, and fail invalid credentials")
    void shouldAuthenticateUserAndManageSession() {
        authService.register("alice_smith", "alicePass", "Alice Smith", "alice@example.com");

        User loggedIn = authService.login("alice_smith", "alicePass");
        assertThat(loggedIn.getUsername()).isEqualTo("alice_smith");
        assertThat(userSession.isLoggedIn()).isTrue();
        assertThat(userSession.getCurrentUser()).contains(loggedIn);

        authService.logout();
        assertThat(userSession.isLoggedIn()).isFalse();
        assertThat(userSession.getCurrentUser()).isEmpty();

        assertThatThrownBy(() -> authService.login("alice_smith", "wrongPass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");
    }
}
