package com.sumer.sumerstores.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {
    private Long addressId;
    private String name;
    private String street;
    private String city;
    private String province;
    private String zipCode;
    private String phoneNumber;
}
