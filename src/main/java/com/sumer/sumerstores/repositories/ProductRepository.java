package com.sumer.sumerstores.repositories;


import com.sumer.sumerstores.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByNameContainingOrDescriptionContaining(String searchValue, String searchValue1);

    List<Product> findByCategoryId(Long categoryId);
    @Query("SELECT p FROM Product p JOIN p.brand b WHERE b.name = :brandName")
    List<Product> findByBrandName(@Param("brandName") String brandName);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product fetchProductById(@Param("id") Long id);


}
