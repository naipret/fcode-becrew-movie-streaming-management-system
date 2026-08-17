package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("CategoryService Test Suite")
class CategoryServiceTest {

    @TempDir
    Path tempDir;

    private CategoryRepository categoryRepository;
    private MovieRepository movieRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        Path catFile = tempDir.resolve("categories.csv");
        Path movieFile = tempDir.resolve("movies.csv");

        categoryRepository = new CategoryRepository(catFile.toString());
        movieRepository = new MovieRepository(movieFile.toString());
        categoryService = new CategoryService(categoryRepository, movieRepository);
    }

    @Test
    @DisplayName("Should create category with auto-generated ID and reject duplicate name")
    void shouldCreateCategoryAndRejectDuplicate() {
        Category created = categoryService.createCategory("Sci-Fi", "Science fiction");
        assertThat(created.getId()).isEqualTo("CAT-01");
        assertThat(created.getName()).isEqualTo("Sci-Fi");

        assertThatThrownBy(() -> categoryService.createCategory("sci-fi", "Another sci fi"))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Category with name 'sci-fi' already exists");
    }

    @Test
    @DisplayName("Should update category and reject duplicate name with other category")
    void shouldUpdateCategory() {
        Category cat1 = categoryService.createCategory("Action", "Action movies");
        categoryService.createCategory("Drama", "Drama movies");

        Category updated = categoryService.updateCategory(cat1.getId(), "Action & Thriller", "Updated desc");
        assertThat(updated.getName()).isEqualTo("Action & Thriller");

        assertThatThrownBy(() -> categoryService.updateCategory(cat1.getId(), "Drama", "Conflict"))
                .isInstanceOf(DuplicateEntityException.class);

        assertThatThrownBy(() -> categoryService.updateCategory("CAT-999", "Name", "Desc"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete category when no movies attached and block deletion when movies exist")
    void shouldEnforceReferentialIntegrityOnDelete() {
        Category cat = categoryService.createCategory("Sci-Fi", "Science fiction");

        // Attach a movie to this category
        Movie movie = new Movie(
                "MOV-001", "Inception", cat.getId(), "Christopher Nolan",
                Arrays.asList("Leonardo DiCaprio"), 2010, 148, 8.8, 1000L, 200L, "Synopsis"
        );
        movieRepository.save(movie);

        // Attempting to delete category should fail with ValidationException
        assertThatThrownBy(() -> categoryService.deleteCategory(cat.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("associated with it");

        // Remove movie, then delete category should succeed
        movieRepository.deleteById(movie.getId());
        categoryService.deleteCategory(cat.getId());

        Optional<Category> found = categoryService.getCategoryById(cat.getId());
        assertThat(found).isEmpty();
    }
}
