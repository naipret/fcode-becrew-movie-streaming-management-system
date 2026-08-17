package com.moviestreaming.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.model.User;
import com.moviestreaming.model.UserRole;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("3-Tier Domain Validators Test Suite")
class ValidatorsTest {

    @Test
    @DisplayName("CategoryValidator should accept valid category and reject invalid fields")
    void shouldValidateCategory() {
        CategoryValidator validator = new CategoryValidator();

        Category valid = new Category("CAT-01", "Action", "High intensity films");
        assertThatCode(() -> validator.validate(valid)).doesNotThrowAnyException();

        Category invalid = new Category("CAT-01", "", "");
        assertThatThrownBy(() -> validator.validate(invalid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category name cannot be empty")
                .hasMessageContaining("Category description cannot be empty");
    }

    @Test
    @DisplayName("MovieValidator should validate all field constraints, ranges, and actors")
    void shouldValidateMovie() {
        MovieValidator validator = new MovieValidator();

        Movie valid = new Movie(
                "MOV-001", "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 8.8, 1000L, 200L, "Synopsis"
        );
        assertThatCode(() -> validator.validate(valid)).doesNotThrowAnyException();

        Movie invalidRating = new Movie(
                "MOV-001", "Inception", "CAT-01", "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 11.0, 1000L, 200L, "Synopsis"
        );
        assertThatThrownBy(() -> validator.validate(invalidRating))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Rating must be between 0.0 and 10.0");

        Movie missingActors = new Movie(
                "MOV-001", "Inception", "CAT-01", "Christopher Nolan",
                Collections.emptyList(), 2010, 148, 8.8, 1000L, 200L, "Synopsis"
        );
        assertThatThrownBy(() -> validator.validate(missingActors))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one actor must be specified");
    }

    @Test
    @DisplayName("UserValidator should validate username regex, email format, and password length")
    void shouldValidateUser() {
        UserValidator validator = new UserValidator();

        User valid = new User("USR-001", "valid_user", "password123", "Valid User", "user@domain.com", UserRole.USER, LocalDateTime.now());
        assertThatCode(() -> validator.validate(valid)).doesNotThrowAnyException();

        User invalidEmail = new User("USR-001", "user", "123", "", "not-an-email", UserRole.USER, LocalDateTime.now());
        assertThatThrownBy(() -> validator.validate(invalidEmail))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Password must be at least 6 characters long")
                .hasMessageContaining("Full name cannot be empty")
                .hasMessageContaining("Invalid email address format");
    }
}
