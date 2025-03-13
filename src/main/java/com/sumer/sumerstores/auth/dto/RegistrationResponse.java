package com.sumer.sumerstores.auth.dto;

import com.sumer.sumerstores.auth.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private int code;
    private String message;
    private QueueMessage queueMessage;
    private User user;
    private Object data;

}
