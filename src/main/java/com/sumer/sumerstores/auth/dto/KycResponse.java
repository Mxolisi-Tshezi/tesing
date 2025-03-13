package com.sumer.sumerstores.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KycResponse {
    private int status;
    private String message;
}
