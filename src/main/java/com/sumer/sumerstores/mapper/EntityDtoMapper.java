package com.sumer.sumerstores.mapper;


import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.dto.CategoryDto;
import com.sumer.sumerstores.dto.ProductDto;
import com.sumer.sumerstores.entities.Category;
import com.sumer.sumerstores.entities.OrderItem;
import com.sumer.sumerstores.entities.Product;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {
    public CategoryDto mapCategoryToDtoBasic(Category category){
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    //Product to DTO Basic
    public ProductDto mapProductToDtoBasic(Product product){
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setImageUrl(product.getImageUrl());
        return productDto;
    }



}
