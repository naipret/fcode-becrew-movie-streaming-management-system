package com.moviestreaming.repository;

import com.moviestreaming.model.Category;
import com.moviestreaming.util.CsvSanitizer;
import java.util.Arrays;
import java.util.List;

/**
 * CSV Serializer for Category entity.
 */
public class CategorySerializer implements CsvSerializer<Category, String> {

    @Override
    public String getHeader() {
        return "id|name|description";
    }

    @Override
    public String serialize(Category category) {
        if (category == null) {
            return "";
        }
        return CsvSanitizer.join(Arrays.asList(
                category.getId(),
                category.getName(),
                category.getDescription()
        ));
    }

    @Override
    public Category deserialize(String csvLine) {
        List<String> tokens = CsvSanitizer.split(csvLine);
        if (tokens.size() < 3) {
            throw new IllegalArgumentException("Invalid Category CSV format, expected 3 columns but got: " + tokens.size());
        }
        return new Category(tokens.get(0), tokens.get(1), tokens.get(2));
    }

    @Override
    public String extractId(Category category) {
        return category != null ? category.getId() : null;
    }
}
