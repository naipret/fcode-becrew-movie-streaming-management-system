package com.moviestreaming.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Domain Models Test Suite")
class ModelTest {

    @Test
    @DisplayName("Should verify Category properties and equals/hashCode")
    void shouldVerifyCategoryModel() {
        Category cat1 = new Category("CAT-01", "Sci-Fi", "Science fiction");
        Category cat2 = new Category("CAT-01", "Different", "Different");
        Category cat3 = new Category("CAT-02", "Action", "Action");

        assertThat(cat1.getId()).isEqualTo("CAT-01");
        assertThat(cat1.getName()).isEqualTo("Sci-Fi");
        assertThat(cat1.getDescription()).isEqualTo("Science fiction");
        assertThat(cat1).isEqualTo(cat2);
        assertThat(cat1).isNotEqualTo(cat3);
        assertThat(cat1.hashCode()).isEqualTo(cat2.hashCode());
        assertThat(cat1.toString()).contains("Sci-Fi");
    }

    @Test
    @DisplayName("Should verify Movie encapsulation and properties")
    void shouldVerifyMovieModel() {
        Movie movie = new Movie(
                "MOV-001", "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio", "Joseph Gordon-Levitt"),
                2010, 148, 8.8, 1000L, 200L, "A dream within a dream."
        );

        assertThat(movie.getId()).isEqualTo("MOV-001");
        assertThat(movie.getTitle()).isEqualTo("Inception");
        assertThat(movie.getActors()).containsExactly("Leonardo DiCaprio", "Joseph Gordon-Levitt");
        assertThat(movie.getReleaseYear()).isEqualTo(2010);
        assertThat(movie.getDurationMinutes()).isEqualTo(148);
        assertThat(movie.getRating()).isEqualTo(8.8);
        assertThat(movie.getViewCount()).isEqualTo(1000L);
        assertThat(movie.getFavoriteCount()).isEqualTo(200L);
        assertThat(movie.getSynopsis()).isEqualTo("A dream within a dream.");

        Movie duplicateId = new Movie();
        duplicateId.setId("MOV-001");
        assertThat(movie).isEqualTo(duplicateId);
    }

    @Test
    @DisplayName("Should verify User and UserRole model")
    void shouldVerifyUserModel() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User("USR-001", "john_doe", "pass123", "John Doe", "john@example.com", UserRole.ADMIN, now);

        assertThat(user.getId()).isEqualTo("USR-001");
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getPassword()).isEqualTo("pass123");
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should verify WatchHistoryItem model")
    void shouldVerifyWatchHistoryItemModel() {
        LocalDateTime now = LocalDateTime.now();
        WatchHistoryItem item = new WatchHistoryItem("HIS-001", "USR-001", "MOV-001", 148, 148, now, true);

        assertThat(item.getId()).isEqualTo("HIS-001");
        assertThat(item.getUserId()).isEqualTo("USR-001");
        assertThat(item.getMovieId()).isEqualTo("MOV-001");
        assertThat(item.getWatchedDurationMinutes()).isEqualTo(148);
        assertThat(item.getTotalDurationMinutes()).isEqualTo(148);
        assertThat(item.getLastWatchedTimestamp()).isEqualTo(now);
        assertThat(item.isCompleted()).isTrue();
    }
}
