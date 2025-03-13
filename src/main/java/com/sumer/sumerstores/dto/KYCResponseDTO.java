package com.sumer.sumerstores.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KYCResponseDTO {
    private String status;
    private String message;
    private int code;
    private String consumerId;
    private String firstName;
    private String surname;
    private String idNumber;
    private String passportNo;
    private String birthDate;
    private String enquiryId;
    private String enquiryResultId;
    private String reference;
}

