package com.sumer.sumerstores.dto;


import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    // Create instances of the JAXB-annotated classes
    public KYCRequestDTO createKYCRequestDTO() {
        return new KYCRequestDTO();
    }

    public KYCResponseDTO createKYCResponseDTO() {
        return new KYCResponseDTO();
    }
}

