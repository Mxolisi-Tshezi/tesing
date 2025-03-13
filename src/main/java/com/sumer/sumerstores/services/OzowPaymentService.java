package com.sumer.sumerstores.services;

import com.sumer.sumerstores.auth.repositories.OzowPaymentRepository;
import com.sumer.sumerstores.dto.OzowPaymentDto;
import com.sumer.sumerstores.dto.OzowResponse;
import com.sumer.sumerstores.dto.Response;
import com.sumer.sumerstores.entities.OzowPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

@Service
@Slf4j
public class OzowPaymentService {

    @Value("${ozow.private.key}")
    private String privateKey;

    @Value("${ozow.api.key}")
    private String apiKey;

    private static final String OZOW_URL = "https://api.ozow.com/PostPaymentRequest";

    private final OzowPaymentRepository ozowPaymentRepository;
    private final RestTemplate restTemplate;

    public OzowPaymentService(OzowPaymentRepository ozowPaymentRepository, RestTemplate restTemplate) {
        this.ozowPaymentRepository = ozowPaymentRepository;
        this.restTemplate = restTemplate;
    }

    public String initiateOzowPayment(OzowPaymentDto paymentDto) {
        String inputString = generateInputString(paymentDto);
        String hashCheck = generateRequestHashCheck(inputString);
        OzowPayment payment = OzowPayment.builder()
                .siteCode(paymentDto.getSiteCode())
                .countryCode(paymentDto.getCountryCode())
                .currencyCode(paymentDto.getCurrencyCode())
                .amount(paymentDto.getAmount())
                .transactionReference(paymentDto.getTransactionReference())
                .bankReference(paymentDto.getBankReference())
                .cancelUrl(paymentDto.getCancelUrl())
                .errorUrl(paymentDto.getErrorUrl())
                .successUrl(paymentDto.getSuccessUrl())
                .notifyUrl(paymentDto.getNotifyUrl())
                .isTest(paymentDto.getIsTest())
                .customerIdentifier(paymentDto.getCustomerIdentifier())
                .hashCheck(hashCheck)
                .build();
        ozowPaymentRepository.save(payment);

        // Post to Ozow API and get the response
        OzowResponse ozowResponse = generatePaymentUrl(paymentDto, hashCheck);
        return ozowResponse.getUrl();  // Assuming the response has the payment URL
    }

    private String generateInputString(OzowPaymentDto paymentDto) {
        return String.join("",
                paymentDto.getSiteCode(),
                paymentDto.getCountryCode(),
                paymentDto.getCurrencyCode(),
                paymentDto.getAmount().toString(),
                paymentDto.getTransactionReference(),
                paymentDto.getBankReference(),
                paymentDto.getCancelUrl(),
                paymentDto.getErrorUrl(),
                paymentDto.getSuccessUrl(),
                paymentDto.getNotifyUrl(),
                paymentDto.getIsTest().toString(),
                privateKey
        ).toLowerCase();
    }

    private String generateRequestHashCheck(String inputString) {
        return getSha512Hash(inputString);
    }

    private String getSha512Hash(String stringToHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating SHA-512 hash", e);
        }
    }


    public OzowResponse generatePaymentUrl(OzowPaymentDto paymentDto, String hashCheck) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ApiKey", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("SiteCode", paymentDto.getSiteCode());
            body.add("CountryCode", paymentDto.getCountryCode());
            body.add("CurrencyCode", paymentDto.getCurrencyCode());
            body.add("Amount", paymentDto.getAmount().toString());
            body.add("TransactionReference", paymentDto.getTransactionReference());
            body.add("BankReference", paymentDto.getBankReference());
            body.add("CancelUrl", paymentDto.getCancelUrl());
            body.add("ErrorUrl", paymentDto.getErrorUrl());
            body.add("SuccessUrl", paymentDto.getSuccessUrl());
            body.add("NotifyUrl", paymentDto.getNotifyUrl());
            body.add("IsTest", paymentDto.getIsTest().toString());
            body.add("HashCheck", hashCheck);
            body.add("CustomerIdentifier", paymentDto.getCustomerIdentifier());
            body.add("GenerateShortUrl", true);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<OzowResponse> response = restTemplate.exchange(OZOW_URL, HttpMethod.POST, request, OzowResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                OzowResponse ozowResponse = response.getBody();
                log.info("Ozow API response: {}", ozowResponse);
                return OzowResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Payment URL successfully generated")
                        .paymentRequestId(ozowResponse.getPaymentRequestId())
                        .url(ozowResponse.getUrl())
                        .build();
            } else {
                return OzowResponse.builder()
                        .status(HttpStatus.NO_CONTENT.value())
                        .message("No content received from Ozow API")
                        .build();
            }

        } catch (Exception e) {
            log.error("Error during payment URL generation", e);
            return OzowResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Error generating payment URL: " + e.getMessage())
                    .build();
        }
    }
}
