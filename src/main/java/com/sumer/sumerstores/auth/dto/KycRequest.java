package com.sumer.sumerstores.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycRequest {
    private String enquiryReason;
    private String idNumber;
    private String passportNo;
    private String firstName;
    private String surname;
    private String birthDate;
    private String yourReference;
    private String voucherCode;
}



