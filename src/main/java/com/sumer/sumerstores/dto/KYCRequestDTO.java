package com.sumer.sumerstores.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "KYCRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class KYCRequestDTO {
    @XmlElement(name = "ConnectTicket")
    private String connectTicket;

    @XmlElement(name = "EnquiryReason")
    private String enquiryReason;

    @XmlElement(name = "ProductId")
    private String productId = "152";

    @XmlElement(name = "IdNumber")
    private String idNumber;

    @XmlElement(name = "PassportNo")
    private String passportNo;

    @XmlElement(name = "FirstName")
    private String firstName;

    @XmlElement(name = "Surname")
    private String surname;

    @XmlElement(name = "BirthDate")
    private String birthDate;

    @XmlElement(name = "YourReference")
    private String yourReference;

    @XmlElement(name = "VoucherCode")
    private String voucherCode;
}
