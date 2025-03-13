package com.sumer.sumerstores.services;

import com.sumer.sumerstores.dto.Response;
import com.sumer.sumerstores.entities.Brand;
import com.sumer.sumerstores.exceptions.BrandNotFoundException;
import com.sumer.sumerstores.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public Response createBrand(Brand brand) {

        if (brandRepository.existsByName(brand.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brand.getName() + "' already exists.");
        }
        Brand newBrand = new Brand();
        newBrand.setName(brand.getName());
        newBrand.setImageUrl(brand.getImageUrl());
        brandRepository.save(newBrand);

        return Response.builder()
                .status(200)
                .message("Brand created successfully")
                .build();
    }



    @Override
    public Response getAllBrands() {
        List<Brand> brands = brandRepository.findAll();

        return Response.builder()
                .status(200)
                .message("Brands retrieved successfully")
                .brandList(brands)
                .build();
    }


    @Override
    public Response getBrandById(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand with id " + brandId + " not found"));

        return Response.builder()
                .status(200)
                .message("Brand found successfully")
                .brandList(Collections.singletonList(brand))
                .build();
    }
    @Override
    public Response updateBrand(Long id, Brand updatedBrand) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand with id " + id + " not found"));
        if (brandRepository.existsByName(updatedBrand.getName()) && !existingBrand.getName().equals(updatedBrand.getName())) {
            throw new IllegalArgumentException("Brand with name '" + updatedBrand.getName() + "' already exists.");
        }
        existingBrand.setName(updatedBrand.getName());
        brandRepository.save(existingBrand);

        return Response.builder()
                .status(200)
                .message("Brand updated successfully")
                .build();
    }

    @Override
    public Response deleteBrand(Long id) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand with id " + id + " not found"));
        brandRepository.delete(existingBrand);

        return Response.builder()
                .status(200)
                .message("Brand deleted successfully")
                .build();
    }

}
