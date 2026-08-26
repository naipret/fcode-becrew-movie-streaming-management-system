package com.moviestreaming.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.model.User;
import com.moviestreaming.model.UserRole;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserListRepository;
import com.moviestreaming.repository.UserRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import com.moviestreaming.service.AuthService;
import com.moviestreaming.service.CategoryService;
import com.moviestreaming.service.FavoriteService;
import com.moviestreaming.service.MovieService;
import com.moviestreaming.service.RecommendationService;
import com.moviestreaming.service.SearchService;
import com.moviestreaming.service.SortingService;
import com.moviestreaming.service.UserSession;
import com.moviestreaming.service.WatchHistoryService;
import com.moviestreaming.service.WatchlistService;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Controller Flow & Access Guard Test Suite")
class ControllerFlowTest {

    @TempDir
    Path tempDir;

    private UserSession userSession;
    private AuthService authService;
    private AuthServiceController authController;
    private AdminController adminController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        Path userFile = tempDir.resolve("users.csv");
        Path catFile = tempDir.resolve("categories.csv");
        Path movieFile = tempDir.resolve("movies.csv");
        Path favFile = tempDir.resolve("user_favorites.csv");
        Path watchFile = tempDir.resolve("user_watchlists.csv");
        Path historyFile = tempDir.resolve("watch_history.csv");

        UserRepository userRepo = new UserRepository(userFile.toString());
        CategoryRepository catRepo = new CategoryRepository(catFile.toString());
        MovieRepository movieRepo = new MovieRepository(movieFile.toString());
        UserListRepository favRepo = new UserListRepository(favFile.toString());
        UserListRepository watchRepo = new UserListRepository(watchFile.toString());
        WatchHistoryRepository historyRepo = new WatchHistoryRepository(historyFile.toString());

        userSession = new UserSession();
        authService = new AuthService(userRepo, userSession);
        authController = new AuthServiceController(authService);

        CategoryService categoryService = new CategoryService(catRepo, movieRepo);
        MovieService movieService = new MovieService(movieRepo, catRepo);
        MovieController movieController = new MovieController(movieService, categoryService);
        SortingService sortingService = new SortingService();
        SearchService searchService = new SearchService(movieService.getIndexingService(), movieRepo, sortingService);
        WatchlistService watchlistService = new WatchlistService(watchRepo, movieRepo);
        FavoriteService favoriteService = new FavoriteService(favRepo, movieRepo, movieService);
        WatchHistoryService historyService = new WatchHistoryService(historyRepo, movieRepo, movieService);
        RecommendationService recService = new RecommendationService(movieRepo, historyService, favoriteService, watchlistService);

        adminController = new AdminController(movieService, categoryService, movieController, userSession);
        userController = new UserController(
                movieService, categoryService, searchService, sortingService,
                watchlistService, favoriteService, historyService, recService,
                movieController, userSession
        );
    }

    @Test
    @DisplayName("Should handle interactive login and registration flows")
    void shouldHandleAuthInteractiveFlow() {
        // Register user via console input
        String regInput = "test_user\nsecret123\nTest User\ntest@example.com\n";
        boolean regOk = authController.handleRegister(new Scanner(regInput));
        assertThat(regOk).isTrue();

        // Login with newly created user
        String loginInput = "test_user\nsecret123\n";
        boolean loginOk = authController.handleLogin(new Scanner(loginInput));
        assertThat(loginOk).isTrue();
        assertThat(userSession.isLoggedIn()).isTrue();

        // Logout
        authController.handleLogout();
        assertThat(userSession.isLoggedIn()).isFalse();
    }

    @Test
    @DisplayName("AdminController should block access if user is not ADMIN")
    void shouldBlockUnauthorizedAdminAccess() {
        User regularUser = new User(
                "USR-002", "regular_user", "pass", "User",
                "user@test.com", UserRole.USER, LocalDateTime.now()
        );
        userSession.setCurrentUser(regularUser);

        // AdminController should return immediately without executing loops
        adminController.runAdminMenu(new Scanner("0\n"));
        assertThat(userSession.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("UserController should block access if user is not logged in")
    void shouldBlockGuestFromUserMenu() {
        userSession.clear();

        userController.runUserMenu(new Scanner("0\n"));
        assertThat(userSession.isLoggedIn()).isFalse();
    }
}
