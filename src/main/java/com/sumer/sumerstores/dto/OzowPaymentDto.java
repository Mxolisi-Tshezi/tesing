package com.sumer.sumerstores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OzowPaymentDto {
    private Long id;
    private String siteCode;
    private String countryCode;
    private String currencyCode;
    private BigDecimal amount;
    private String transactionReference;
    private String bankReference;
    private String cancelUrl;
    private String errorUrl;
    private String successUrl;
    private String notifyUrl;
    private Boolean isTest;
    private String customerIdentifier;
}

