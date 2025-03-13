package com.sumer.sumerstores.dto;

import com.sumer.sumerstores.auth.dto.RegistrationRequest;
import lombok.Data;

@Data
public class UserMessage {
    private RegistrationRequest registrationRequest;
    private String action; // e.g., "CREATE_QUOTE", "GET_POLICY"
}
