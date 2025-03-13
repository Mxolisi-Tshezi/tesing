package com.sumer.sumerstores.dto;


import com.sumer.sumerstores.entities.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDetail {

    private Long id;
    private Product product;
    private Long productVariantId;
    private Integer quantity;
    private Double itemPrice;
}
