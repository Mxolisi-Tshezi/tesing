package com.sumer.sumerstores.services;



import com.sumer.sumerstores.dto.Response;
import com.sumer.sumerstores.entities.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    Response createProduct(Long categoryId,Long brandId, MultipartFile image, String name, String description, BigDecimal price);
    Response updateProduct(Long productId, Long categoryId,Long brandId, MultipartFile image, String name, String description, BigDecimal price);
    Response deleteProduct(Long productId);
    Response getProductById(Long productId);

    Response getAllProducts();
    Response getProductsByCategory(Long categoryId);
    Response searchProduct(String searchValue);


    Product fetchProductById(Long productId);

    Response findByBrandName(String brandName);
}
