package com.sumer.sumerstores.dto;

import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.entities.Category;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private int code;            // HTTP status code (e.g., 200, 400, 404)
    private String message;      // A message related to the action (e.g., success or error message)
    private Category data; // The AddressRequest that was used for the creation (optional)
    private QueueMessage queueMessage;
    private int status;
    private List<ProductDto> productList;
}
