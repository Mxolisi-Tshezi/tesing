package com.sumer.sumerstores.services;

import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.dto.CategoryDto;
import com.sumer.sumerstores.dto.Response;
import com.sumer.sumerstores.entities.Category;
import com.sumer.sumerstores.exceptions.NotFoundException;
import com.sumer.sumerstores.mapper.EntityDtoMapper;
import com.sumer.sumerstores.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final EntityDtoMapper entityDtoMapper;
    private final QueueService queueService;

    @Override
    public Response createCategory(CategoryDto categoryRequest) {
        // Check if category with the same name exists
        Category existing = categoryRepo.findByName(categoryRequest.getName());
        if (existing != null) {
            return Response.builder()
                    .status(400)
                    .message("Category with this name already exists!")
                    .build();
        }

        try {
            // Create new Category
            Category category = new Category();
            category.setName(categoryRequest.getName());

            // Save the new category
            categoryRepo.save(category);

            // Send message to the queue for category creation
            queueService.sendMessageToQueue(category.getId(),"CREATE_CATEGORY");

            // Return a success response with queue message information
            return Response.builder()
                    .status(200)
                    .queueMessage(QueueMessage.builder()
                            .action("CREATE_CATEGORY")
                            .status("Message sent to queue")
                            .build()
                    )
                    .message("Category created successfully!")
                    .category(categoryRequest)
                    .build();
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            throw new RuntimeException("Category creation failed", e);
        }
    }

    @Override
    public Response updateCategory(Long categoryId, CategoryDto categoryRequest) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));
        category.setName(categoryRequest.getName());
        categoryRepo.save(category);

        // Send message to the queue for category update
        queueService.sendMessageToQueue(category.getId(),"UPDATE_CATEGORY");

        return Response.builder()
                .status(200)
                .message("Category updated successfully!")
                .category(categoryRequest)
                .queueMessage(QueueMessage.builder()
                        .action("UPDATE_CATEGORY")
                        .status("Message sent to queue")
                        .build()
                )
                .build();
    }

    @Override
    public Response deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));
        categoryRepo.delete(category);

        // Send message to the queue for category deletion
        queueService.sendMessageToQueue(categoryId,"DELETE_CATEGORY");

        return Response.builder()
                .status(200)
                .message("Category deleted successfully!")
                .queueMessage(QueueMessage.builder()
                        .action("DELETE_CATEGORY")
                        .status("Message sent to queue")
                        .build()
                )
                .build();
    }

    @Override
    public Response getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        List<CategoryDto> categoryDtoList = categories.stream()
                .map(entityDtoMapper::mapCategoryToDtoBasic)
                .collect(Collectors.toList());

        return  Response.builder()
                .status(200)
                .categoryList(categoryDtoList)
                .build();
    }
    @Override
    public Response getCategoryById(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));
        CategoryDto categoryDto = entityDtoMapper.mapCategoryToDtoBasic(category);

        return Response.builder()
                .status(200)
                .message("Categories retrieved successfully!")
                .queueMessage(QueueMessage.builder()
                        .action("FIND_BY_CATEGORY")
                        .status("Message sent to queue")
                        .build()
                )
                .category(categoryDto)
                .build();
    }
}
