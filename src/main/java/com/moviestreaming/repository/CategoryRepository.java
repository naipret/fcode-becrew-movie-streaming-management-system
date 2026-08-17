package com.moviestreaming.repository;

import com.moviestreaming.model.Category;

/**
 * Concrete file-backed repository for Category entities.
 */
public class CategoryRepository extends GenericFileRepository<Category, String> {

    public CategoryRepository(String filePath) {
        super(filePath, new CategorySerializer());
    }
}
