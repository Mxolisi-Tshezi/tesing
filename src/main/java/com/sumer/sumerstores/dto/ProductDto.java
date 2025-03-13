package com.sumer.sumerstores.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private  String description;
    private BigDecimal price;
    private String imageUrl;
    private CategoryDto category;
    private String deviceMake;
    private String deviceModel;
    private boolean devicePrepaid;
    private String mie;

    public static class KYCRequest {
        private String connectTicket;
        private String enquiryReason;
        private String productId;
        private String idNumber;
        private String firstName;
        private String surname;
        private String birthDate;
        private String reference;
        private String voucherCode;
    }
}
