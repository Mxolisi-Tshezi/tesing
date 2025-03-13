package com.sumer.sumerstores.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueMessage {
    private Long id;
    private String action;
    private String status;
}

