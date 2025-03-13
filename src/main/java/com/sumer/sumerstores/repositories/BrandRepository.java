package com.sumer.sumerstores.repositories;

import com.sumer.sumerstores.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByName(String name);
}

