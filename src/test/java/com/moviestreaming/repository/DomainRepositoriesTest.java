package com.moviestreaming.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.User;
import com.moviestreaming.model.WatchHistoryItem;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Domain Repositories & Sample Data Loading Test Suite")
class DomainRepositoriesTest {

    @Test
    @DisplayName("Should load all 10 sample categories from CSV correctly")
    void shouldLoadSampleCategories() {
        String path = Paths.get(AppConstants.DATA_DIR, AppConstants.CATEGORIES_FILE).toString();
        CategoryRepository repo = new CategoryRepository(path);

        assertThat(repo.count()).isGreaterThanOrEqualTo(10);
        Optional<Category> sciFi = repo.findById("CAT-01");
        assertThat(sciFi).isPresent();
        assertThat(sciFi.get().getName()).isEqualTo("Sci-Fi");
    }

    @Test
    @DisplayName("Should load 50+ sample movies from CSV with all fields parsed")
    void shouldLoadSampleMovies() {
        String path = Paths.get(AppConstants.DATA_DIR, AppConstants.MOVIES_FILE).toString();
        MovieRepository repo = new MovieRepository(path);

        assertThat(repo.count()).isGreaterThanOrEqualTo(50);
        Optional<Movie> inception = repo.findById("MOV-001");
        assertThat(inception).isPresent();
        assertThat(inception.get().getTitle()).isEqualTo("Inception");
        assertThat(inception.get().getActors()).contains("Leonardo DiCaprio");
        assertThat(inception.get().getRating()).isEqualTo(8.8);
    }

    @Test
    @DisplayName("Should load users and find by username/email")
    void shouldLoadSampleUsers() {
        String path = Paths.get(AppConstants.DATA_DIR, AppConstants.USERS_FILE).toString();
        UserRepository repo = new UserRepository(path);

        assertThat(repo.count()).isGreaterThanOrEqualTo(4);
        Optional<User> admin = repo.findByUsername("admin");
        assertThat(admin).isPresent();
        assertThat(admin.get().getFullName()).isEqualTo("System Administrator");

        Optional<User> john = repo.findByEmail("john.doe@gmail.com");
        assertThat(john).isPresent();
        assertThat(john.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("Should load watch history and query by user ID")
    void shouldLoadWatchHistory() {
        String path = Paths.get(AppConstants.DATA_DIR, AppConstants.WATCH_HISTORY_FILE).toString();
        WatchHistoryRepository repo = new WatchHistoryRepository(path);

        assertThat(repo.count()).isGreaterThanOrEqualTo(8);
        List<WatchHistoryItem> user2History = repo.findByUserId("USR-002");
        assertThat(user2History).isNotEmpty();
    }
}
