package com.moviestreaming.service;

import com.moviestreaming.exception.DuplicateEntityException;
import com.moviestreaming.exception.EntityNotFoundException;
import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Category;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.validator.CategoryValidator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing Category CRUD operations, duplicate checks, and referential integrity.
 */
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MovieRepository movieRepository;
    private final CategoryValidator validator;

    public CategoryService(CategoryRepository categoryRepository, MovieRepository movieRepository) {
        if (categoryRepository == null || movieRepository == null) {
            throw new IllegalArgumentException("Repositories must not be null");
        }
        this.categoryRepository = categoryRepository;
        this.movieRepository = movieRepository;
        this.validator = new CategoryValidator();
    }

    /**
     * Creates and persists a new Category with an auto-generated ID.
     *
     * @param name        the category name
     * @param description the category description
     * @return the created Category
     */
    public Category createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }

        String trimmedName = name.trim();
        checkNameUniqueness(trimmedName, null);

        String nextId = generateNextId();
        Category category = new Category(nextId, trimmedName, description != null ? description.trim() : "");
        validator.validate(category);

        return categoryRepository.save(category);
    }

    /**
     * Updates an existing Category.
     *
     * @param id          the category ID
     * @param newName     the new category name
     * @param newDesc     the new description
     * @return the updated Category
     */
    public Category updateCategory(String id, String newName, String newDesc) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category", id));

        if (newName != null && !newName.trim().isEmpty()) {
            String trimmedName = newName.trim();
            checkNameUniqueness(trimmedName, id);
            category.setName(trimmedName);
        }

        if (newDesc != null) {
            category.setDescription(newDesc.trim());
        }

        validator.validate(category);
        return categoryRepository.save(category);
    }

    /**
     * Deletes a Category by ID if no movies are linked to it.
     *
     * @param id the category ID
     */
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category", id));

        // Referential Integrity: check if movies are linked
        List<Movie> linkedMovies = movieRepository.findAll().stream()
                .filter(m -> m.getCategoryId() != null && m.getCategoryId().equalsIgnoreCase(id))
                .collect(Collectors.toList());

        if (!linkedMovies.isEmpty()) {
            throw new ValidationException(String.format(
                    "Cannot delete category '%s' (%s) because %d movie(s) are associated with it.",
                    category.getName(), id, linkedMovies.size()
            ));
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Finds a Category by ID.
     *
     * @param id the category ID
     * @return Optional containing Category if found
     */
    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    /**
     * Retrieves all categories.
     *
     * @return list of categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    private void checkNameUniqueness(String name, String excludeId) {
        boolean duplicate = categoryRepository.findAll().stream()
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .anyMatch(c -> c.getName() != null && c.getName().equalsIgnoreCase(name));

        if (duplicate) {
            throw new DuplicateEntityException("Category", "name", name);
        }
    }

    private synchronized String generateNextId() {
        int maxIndex = categoryRepository.findAll().stream()
                .map(Category::getId)
                .filter(id -> id != null && id.startsWith("CAT-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(4));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("CAT-%02d", maxIndex + 1);
    }
}
