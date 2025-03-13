package com.sumer.sumerstores.dto;

import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.entities.Product;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private int code;            // HTTP status code (e.g., 200, 400, 404)
    private String message;      // A message related to the action (e.g., success or error message)
    private Product data; // The AddressRequest that was used for the creation (optional)
    private QueueMessage queueMessage;
    private int status;
}
