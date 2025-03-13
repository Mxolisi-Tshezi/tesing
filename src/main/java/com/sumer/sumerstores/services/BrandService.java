package com.sumer.sumerstores.services;

import com.sumer.sumerstores.dto.Response;
import com.sumer.sumerstores.entities.Brand;

public interface BrandService {
    Response createBrand(Brand brand);
    Response getAllBrands();

    Response getBrandById(Long id);

    Response updateBrand(Long id, Brand updatedBrand);

    Response deleteBrand(Long id);
}

