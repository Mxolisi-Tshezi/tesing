package com.sumer.sumerstores.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String deviceMake;
    private String deviceModel;
    private boolean isDevicePrepaid;
    private String mie;
    @Column(name = "provider", nullable = false)
    private String provider = "VODACOM";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "created_at")
    private final LocalDateTime createdAt = LocalDateTime.now();
}
