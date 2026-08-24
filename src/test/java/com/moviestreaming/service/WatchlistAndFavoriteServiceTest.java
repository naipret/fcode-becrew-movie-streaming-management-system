package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserListRepository;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Watchlist and Favorite Services Test Suite")
class WatchlistAndFavoriteServiceTest {

    @TempDir
    Path tempDir;

    private MovieRepository movieRepository;
    private CategoryRepository categoryRepository;
    private MovieService movieService;
    private UserListRepository watchlistRepo;
    private UserListRepository favoriteRepo;
    private WatchlistService watchlistService;
    private FavoriteService favoriteService;
    private Movie sampleMovie;

    @BeforeEach
    void setUp() {
        Path catFile = tempDir.resolve("categories.csv");
        Path movieFile = tempDir.resolve("movies.csv");
        Path watchlistFile = tempDir.resolve("user_watchlists.csv");
        Path favoriteFile = tempDir.resolve("user_favorites.csv");

        categoryRepository = new CategoryRepository(catFile.toString());
        movieRepository = new MovieRepository(movieFile.toString());
        categoryRepository.save(new Category("CAT-01", "Sci-Fi", "Science fiction"));

        movieService = new MovieService(movieRepository, categoryRepository);
        sampleMovie = movieService.createMovie(new Movie(
                null, "Inception", "CAT-01", "Christopher Nolan",
                Collections.singletonList("Leonardo DiCaprio"), 2010, 148, 8.8, 0, 0, "Dream espionage"
        ));

        watchlistRepo = new UserListRepository(watchlistFile.toString());
        favoriteRepo = new UserListRepository(favoriteFile.toString());

        watchlistService = new WatchlistService(watchlistRepo, movieRepository);
        favoriteService = new FavoriteService(favoriteRepo, movieRepository, movieService);
    }

    @Test
    @DisplayName("Should add, query, and remove movie from Watchlist")
    void shouldManageWatchlist() {
        watchlistService.addToWatchlist("USR-001", sampleMovie.getId());
        assertThat(watchlistService.isInWatchlist("USR-001", sampleMovie.getId())).isTrue();

        List<Movie> list = watchlistService.getWatchlist("USR-001");
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTitle()).isEqualTo("Inception");

        assertThatThrownBy(() -> watchlistService.addToWatchlist("USR-001", sampleMovie.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already in your watchlist");

        watchlistService.removeFromWatchlist("USR-001", sampleMovie.getId());
        assertThat(watchlistService.isInWatchlist("USR-001", sampleMovie.getId())).isFalse();
        assertThat(watchlistService.getWatchlist("USR-001")).isEmpty();
    }

    @Test
    @DisplayName("Should manage Favorites and sync Movie favoriteCount")
    void shouldManageFavoritesAndSyncCounter() {
        favoriteService.addToFavorites("USR-001", sampleMovie.getId());
        assertThat(favoriteService.isFavorite("USR-001", sampleMovie.getId())).isTrue();

        // Favorite count should be 1
        Movie updated = movieService.getMovieById(sampleMovie.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getFavoriteCount()).isEqualTo(1L);

        // Remove favorite
        favoriteService.removeFromFavorites("USR-001", sampleMovie.getId());
        assertThat(favoriteService.isFavorite("USR-001", sampleMovie.getId())).isFalse();

        // Favorite count should be decremented back to 0
        Movie decremented = movieService.getMovieById(sampleMovie.getId()).orElse(null);
        assertThat(decremented).isNotNull();
        assertThat(decremented.getFavoriteCount()).isEqualTo(0L);
    }
}
