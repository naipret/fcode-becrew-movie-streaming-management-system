package com.moviestreaming;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.exception.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Application Foundation & Exceptions Test Suite")
class AppTest {

    @Test
    @DisplayName("Should verify core application constants")
    void shouldVerifyApplicationConstants() {
        assertThat(AppConstants.APP_NAME).contains("Netflix CLI");
        assertThat(AppConstants.WEIGHT_RATING + AppConstants.WEIGHT_VIEWS + AppConstants.WEIGHT_FAVORITES)
                .isEqualTo(1.0);
        assertThat(AppConstants.DEFAULT_PAGE_SIZE).isEqualTo(10);
        assertThat(AppConstants.MAX_UNDO_REDO_STACK_SIZE).isEqualTo(30);
    }

    @Test
    @DisplayName("Should correctly construct and handle EntityNotFoundException")
    void shouldHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Movie", "MOV-999");
        assertThat(exception.getMessage()).isEqualTo("Movie with ID 'MOV-999' was not found.");
    }

    @Test
    @DisplayName("Should correctly construct and handle DuplicateEntityException")
    void shouldHandleDuplicateEntityException() {
        DuplicateEntityException exception = new DuplicateEntityException("Category", "name", "Action");
        assertThat(exception.getMessage()).isEqualTo("Category with name 'Action' already exists.");
    }

    @Test
    @DisplayName("Should correctly collect multiple validation errors in ValidationException")
    void shouldCollectValidationErrors() {
        ValidationException exception = new ValidationException(Arrays.asList("Title is required", "Rating out of range"));
        assertThat(exception.getErrors()).containsExactly("Title is required", "Rating out of range");
        assertThat(exception.getMessage()).contains("Title is required", "Rating out of range");
    }

    @Test
    @DisplayName("Should run main bootstrap method and exit gracefully")
    void shouldExecuteMainMethod() {
        InputStream originalIn = System.in;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8));
            System.setIn(in);
            App.main(new String[]{});
        } finally {
            System.setIn(originalIn);
        }
    }
}
