package com.sumer.sumerstores.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OzowResponse {

    @JsonProperty("PaymentRequestId")
    private String paymentRequestId;
    @JsonProperty("URL")
    private String url;
    @JsonProperty("ErrorMessage")
    private String errorMessage;
    @JsonProperty("data")
    private OzowPaymentDto data;
    @JsonProperty("status")
    private int status;
    @JsonProperty("message")
    private String message;


}

