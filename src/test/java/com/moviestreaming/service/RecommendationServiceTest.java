package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserListRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecommendationService Content-Based & Cold-Start Test Suite")
class RecommendationServiceTest {

    private RecommendationService recommendationService;
    private MovieRepository movieRepository;
    private FavoriteService favoriteService;
    private WatchHistoryService historyService;

    @BeforeEach
    void setUp() {
        String catPath = Paths.get(AppConstants.DATA_DIR, AppConstants.CATEGORIES_FILE).toString();
        String moviePath = Paths.get(AppConstants.DATA_DIR, AppConstants.MOVIES_FILE).toString();
        String favPath = Paths.get(AppConstants.DATA_DIR, AppConstants.USER_FAVORITES_FILE).toString();
        String watchPath = Paths.get(AppConstants.DATA_DIR, AppConstants.USER_WATCHLISTS_FILE).toString();
        String historyPath = Paths.get(AppConstants.DATA_DIR, AppConstants.WATCH_HISTORY_FILE).toString();

        CategoryRepository categoryRepo = new CategoryRepository(catPath);
        movieRepository = new MovieRepository(moviePath);
        MovieService movieService = new MovieService(movieRepository, categoryRepo);

        UserListRepository favoriteRepo = new UserListRepository(favPath);
        UserListRepository watchlistRepo = new UserListRepository(watchPath);
        WatchHistoryRepository historyRepo = new WatchHistoryRepository(historyPath);

        favoriteService = new FavoriteService(favoriteRepo, movieRepository, movieService);
        WatchlistService watchlistService = new WatchlistService(watchlistRepo, movieRepository);
        historyService = new WatchHistoryService(historyRepo, movieRepository, movieService);

        recommendationService = new RecommendationService(
                movieRepository,
                historyService,
                favoriteService,
                watchlistService
        );
    }

    @Test
    @DisplayName("Should return cold-start trending movies for brand new users")
    void shouldReturnTrendingForColdStart() {
        List<Movie> trending = recommendationService.getRecommendationsForUser("USR-NEW-USER", 5);
        assertThat(trending).hasSize(5);
        assertThat(trending.get(0).getViewCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should recommend similar movies given target movie")
    void shouldFindSimilarMovies() {
        // MOV-001 is Inception (Sci-Fi, Christopher Nolan)
        List<Movie> similar = recommendationService.getSimilarMovies("MOV-001", 5);
        assertThat(similar).isNotEmpty();
        assertThat(similar).extracting(Movie::getId).doesNotContain("MOV-001");
    }
}
