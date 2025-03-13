package com.sumer.sumerstores.services;


import com.sumer.sumerstores.dto.CategoryDto;
import com.sumer.sumerstores.dto.Response;

public interface CategoryService {

    Response createCategory(CategoryDto categoryRequest);
    Response updateCategory(Long categoryId, CategoryDto categoryRequest);
    Response getAllCategories();
    Response getCategoryById(Long categoryId);
    Response deleteCategory(Long categoryId);
}
