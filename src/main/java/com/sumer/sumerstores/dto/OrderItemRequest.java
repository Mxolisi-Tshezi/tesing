package com.sumer.sumerstores.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    private Long productId;
    private Long productVariantId;
    private Double discount;
    private Integer quantity;
}
