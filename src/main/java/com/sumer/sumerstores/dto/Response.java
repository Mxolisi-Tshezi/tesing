package com.sumer.sumerstores.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.entities.Brand;
import com.sumer.sumerstores.entities.Product;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private int status;
    private String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private String token;
    private String role;
    private String  expirationTime;

    private int totalPage;
    private long totalElement;
    private CategoryDto category;
    private List<CategoryDto> categoryList;
    private QueueMessage queueMessage;
    private List<OrderDetails> orderDetailsList;

    private Product product;
    private List<ProductDto> productList;
    private User user;

    private List<Brand> brandList;


}
