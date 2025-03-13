package com.sumer.sumerstores.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ozow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OzowPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_code", nullable = false)
    private String siteCode;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_reference", nullable = false)
    private String transactionReference;

    @Column(name = "bank_reference", nullable = false)
    private String bankReference;

    @Column(name = "cancel_url")
    private String cancelUrl;

    @Column(name = "error_url")
    private String errorUrl;

    @Column(name = "success_url")
    private String successUrl;

    @Column(name = "notify_url")
    private String notifyUrl;

    @Column(name = "is_test", nullable = false)
    private Boolean isTest;

    // Optional fields, if necessary
    @Column(name = "customer_identifier")
    private String customerIdentifier;

    @Column(name = "hash_check", nullable = false)
    private String hashCheck;
}

