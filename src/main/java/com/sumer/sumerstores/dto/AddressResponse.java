package com.sumer.sumerstores.dto;



import com.sumer.sumerstores.auth.dto.QueueMessage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private int code;            // HTTP status code (e.g., 200, 400, 404)
    private String message;      // A message related to the action (e.g., success or error message)
    private AddressRequest data; // The AddressRequest that was used for the creation (optional)
    private QueueMessage queueMessage; // Queue message details (optional)
}

