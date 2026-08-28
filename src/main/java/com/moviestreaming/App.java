package com.moviestreaming;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.controller.AdminController;
import com.moviestreaming.controller.AuthServiceController;
import com.moviestreaming.controller.MainMenuController;
import com.moviestreaming.controller.MovieController;
import com.moviestreaming.controller.UserController;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserListRepository;
import com.moviestreaming.repository.UserRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import com.moviestreaming.service.AnalyticsReportService;
import com.moviestreaming.service.AuthService;
import com.moviestreaming.service.CategoryService;
import com.moviestreaming.service.FavoriteService;
import com.moviestreaming.service.FilterService;
import com.moviestreaming.service.IndexingService;
import com.moviestreaming.service.MovieRankingEngine;
import com.moviestreaming.service.MovieService;
import com.moviestreaming.service.RecommendationService;
import com.moviestreaming.service.SearchService;
import com.moviestreaming.service.SortingService;
import com.moviestreaming.service.UserSession;
import com.moviestreaming.service.WatchHistoryService;
import com.moviestreaming.service.WatchlistService;
import com.moviestreaming.service.WatchlistUndoRedoService;
import java.io.File;
import java.util.Scanner;

/**
 * Main Application Bootstrap Entry Point for Netflix CLI Movie Streaming Management System.
 * Constructs dependency injection graph across Repository, Service, and Controller layers.
 */
public class App {

    private App() {
        // Utility class constructor
    }

    /**
     * Main entry point starting the interactive console application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        String dataDir = AppConstants.DATA_DIR;

        // 1. Initialize Repositories (Storage Layer)
        CategoryRepository categoryRepo = new CategoryRepository(dataDir + File.separator + AppConstants.CATEGORIES_FILE);
        MovieRepository movieRepo = new MovieRepository(dataDir + File.separator + AppConstants.MOVIES_FILE);
        UserRepository userRepo = new UserRepository(dataDir + File.separator + AppConstants.USERS_FILE);
        WatchHistoryRepository historyRepo = new WatchHistoryRepository(dataDir + File.separator + AppConstants.WATCH_HISTORY_FILE);
        UserListRepository watchlistRepo = new UserListRepository(dataDir + File.separator + AppConstants.USER_WATCHLISTS_FILE);
        UserListRepository favoritesRepo = new UserListRepository(dataDir + File.separator + AppConstants.USER_FAVORITES_FILE);

        // 2. Initialize Services (Domain Business Layer)
        CategoryService categoryService = new CategoryService(categoryRepo, movieRepo);
        IndexingService indexingService = new IndexingService();
        indexingService.initialize(movieRepo.findAll(), categoryRepo.findAll());

        MovieService movieService = new MovieService(movieRepo, categoryRepo, indexingService);
        SortingService sortingService = new SortingService();
        SearchService searchService = new SearchService(indexingService, movieRepo, sortingService);
        WatchlistService watchlistService = new WatchlistService(watchlistRepo, movieRepo);
        FavoriteService favoriteService = new FavoriteService(favoritesRepo, movieRepo, movieService);
        WatchHistoryService historyService = new WatchHistoryService(historyRepo, movieRepo, movieService);
        RecommendationService recommendationService =
                new RecommendationService(movieRepo, historyService, favoriteService, watchlistService);
        WatchlistUndoRedoService undoRedoService = new WatchlistUndoRedoService(AppConstants.MAX_UNDO_REDO_STACK_SIZE);
        FilterService filterService = new FilterService(movieRepo);
        MovieRankingEngine rankingEngine = new MovieRankingEngine(movieRepo);
        AnalyticsReportService analyticsReportService =
                new AnalyticsReportService(historyRepo, movieRepo, categoryRepo, userRepo);
        UserSession userSession = new UserSession();
        AuthService authService = new AuthService(userRepo, userSession);

        // 3. Initialize Controllers & Views
        MovieController movieController = new MovieController(movieService, categoryService);
        AuthServiceController authController = new AuthServiceController(authService);
        UserController userController = new UserController(
                movieService, categoryService, searchService, sortingService,
                watchlistService, favoriteService, historyService, recommendationService,
                undoRedoService, filterService, rankingEngine, analyticsReportService,
                movieController, userSession
        );
        AdminController adminController = new AdminController(
                movieService, categoryService, analyticsReportService, rankingEngine,
                movieController, userSession
        );
        MainMenuController mainController = new MainMenuController(
                authController, userController, adminController, userSession
        );

        // 4. Start Interactive Lifecycle
        Scanner scanner = new Scanner(System.in);
        mainController.start(scanner);
    }
}
