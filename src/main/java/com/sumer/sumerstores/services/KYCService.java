package com.sumer.sumerstores.services;

import com.sumer.sumerstores.dto.KYCRequestDTO;
import com.sumer.sumerstores.dto.KYCResponseDTO;
import com.sumer.sumerstores.services.XDSLoginService;
import jakarta.xml.bind.JAXBException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;


@Service
public class KYCService extends WebServiceGatewaySupport {

    private static final String SOAP_ENDPOINT_URL = "https://www.uat.xds.co.za/NewPortal/KYC/KYCEnquirymatch";
    private final XDSLoginService xdsLoginService;
    private final Jaxb2Marshaller marshaller;

    public KYCService(XDSLoginService xdsLoginService, Jaxb2Marshaller marshaller) {
        this.xdsLoginService = xdsLoginService;
        this.marshaller = marshaller;
        this.setMarshaller(marshaller);
        this.setUnmarshaller(marshaller);
    }

    public KYCResponseDTO performKYC(KYCRequestDTO requestDTO)  {
        try {
            String authTicket = xdsLoginService.getAuthenticationTicket("username", "password");
            KYCRequestDTO request = buildRequest(requestDTO, authTicket);
            Object response = getWebServiceTemplate().marshalSendAndReceive(SOAP_ENDPOINT_URL, request);
            return convertToKYCResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return KYCResponseDTO.builder()
                    .status("ERROR")
                    .message("Failed to perform KYC verification")
                    .code(500)
                    .build();
        }
    }

    private KYCRequestDTO buildRequest(KYCRequestDTO requestDTO, String authTicket) {
        KYCRequestDTO request = new KYCRequestDTO();
        request.setConnectTicket(authTicket);
        request.setEnquiryReason(requestDTO.getEnquiryReason());
        request.setProductId("152");
        request.setIdNumber(requestDTO.getIdNumber());
        request.setPassportNo(requestDTO.getPassportNo());
        request.setFirstName(requestDTO.getFirstName());
        request.setSurname(requestDTO.getSurname());
        request.setBirthDate(requestDTO.getBirthDate());
        request.setYourReference(requestDTO.getYourReference());
        request.setVoucherCode(requestDTO.getVoucherCode());
        return request;
    }

    private KYCResponseDTO convertToKYCResponse(Object response) {
        KYCResponseDTO soapResponse = (KYCResponseDTO) response;

        return KYCResponseDTO.builder()
                .consumerId(soapResponse.getConsumerId())
                .firstName(soapResponse.getFirstName())
                .surname(soapResponse.getSurname())
                .idNumber(soapResponse.getIdNumber())
                .passportNo(soapResponse.getPassportNo())
                .birthDate(soapResponse.getBirthDate())
                .enquiryId(soapResponse.getEnquiryId())
                .enquiryResultId(soapResponse.getEnquiryResultId())
                .reference(soapResponse.getReference())
                .build();
    }
}
