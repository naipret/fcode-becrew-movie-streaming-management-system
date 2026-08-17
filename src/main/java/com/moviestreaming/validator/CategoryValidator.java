package com.moviestreaming.validator;

import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain validator for Category entities.
 */
public class CategoryValidator implements Validator<Category> {

    @Override
    public void validate(Category category) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (category == null) {
            throw new ValidationException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            errors.add("Category name cannot be empty");
        } else if (category.getName().trim().length() < 2 || category.getName().trim().length() > 50) {
            errors.add("Category name must be between 2 and 50 characters");
        }

        if (category.getDescription() == null || category.getDescription().trim().isEmpty()) {
            errors.add("Category description cannot be empty");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
