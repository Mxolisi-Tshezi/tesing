package com.sumer.sumerstores.services;


import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.dto.CategoryDto;
import com.sumer.sumerstores.dto.ProductDto;
import com.sumer.sumerstores.dto.Response;
import com.sumer.sumerstores.entities.Brand;
import com.sumer.sumerstores.entities.Category;
import com.sumer.sumerstores.entities.Product;
import com.sumer.sumerstores.exceptions.NotFoundException;
import com.sumer.sumerstores.mapper.EntityDtoMapper;
import com.sumer.sumerstores.repositories.BrandRepository;
import com.sumer.sumerstores.repositories.CategoryRepository;
import com.sumer.sumerstores.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepository;
    private final EntityDtoMapper entityDtoMapper;
    private final AwsS3Service awsS3Service;



    @Override
    public Response createProduct(Long categoryId,Long brandId, MultipartFile image, String name, String description, BigDecimal price) {
        Category category = categoryRepo.findById(categoryId).orElseThrow(() -> new NotFoundException("Category not found"));
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new NotFoundException("Brand not found"));
        String productImageUrl = awsS3Service.saveFileToS3(image);
        Product product = new Product();
        product.setCategory(category);
        product.setBrand(brand);
        product.setPrice(price);
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(productImageUrl);
        product.setDeviceMake(product.getDeviceMake());
        product.setDeviceModel(product.getDeviceModel());
        product.setDevicePrepaid(product.isDevicePrepaid());
        product.setMie(product.getMie());

        productRepo.save(product);
        return Response.builder()
                .status(200)
                .message("Product successfully created")
                .queueMessage(QueueMessage.builder()
                        .action("CREATE_PRODUCT")
                        .status("Message sent to queue")
                        .build())
                .product(product)
                .build();
    }
    @Override
    public Response updateProduct(Long productId, Long categoryId,Long brandId, MultipartFile image, String name, String description, BigDecimal price) {
        Product product = productRepo.findById(productId).orElseThrow(()-> new NotFoundException("Product Not Found"));

        Category category = null;
        String productImageUrl = null;
        Brand brand = null;

        if(categoryId != null ){
             category = categoryRepo.findById(categoryId).orElseThrow(()-> new NotFoundException("Category not found"));
        }
        if(brandId != null ){
            brand = brandRepository.findById(brandId).orElseThrow(()-> new NotFoundException("Brand not found"));
        }
        if (image != null && !image.isEmpty()){
            productImageUrl = awsS3Service.saveFileToS3(image);
        }

        if (category != null) product.setCategory(category);
        if (name != null) product.setName(name);
        if (price != null) product.setPrice(price);
        if (description != null) product.setDescription(description);
        if (productImageUrl != null) product.setImageUrl(productImageUrl);

        productRepo.save(product);
        return Response.builder()
                .status(200)
                .message("Product updated successfully")
                .queueMessage(QueueMessage.builder()
                        .action("UPDATE_PRODUCT")
                        .status("Message sent to queue")
                        .build()
                )
                .build();

    }

    @Override
    public Response deleteProduct(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow(()-> new NotFoundException("Product Not Found"));
        productRepo.delete(product);

        return Response.builder()
                .status(200)
                .message("Product deleted successfully")
                .queueMessage(QueueMessage.builder()
                        .action("DELETE_PRODUCT")
                        .status("Message sent to queue")
                        .build()
                )
                .build();
    }

    @Override
    public Response getProductById(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow(()-> new NotFoundException("Product Not Found"));
        ProductDto productDto = entityDtoMapper.mapProductToDtoBasic(product);
        return Response.builder()
                .status(200)
                .message("Product retrieved by ID successfully")
                .queueMessage(QueueMessage.builder()
                        .action("FIND_BY_ID")
                        .status("Message sent to queue")
                        .build()
                )
                .product(product)
                .build();
    }

    @Override
    public Response getAllProducts() {
        List<ProductDto> productList = productRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Products retrieved successfully")
                .queueMessage(QueueMessage.builder()
                        .action("RETRIEVE_ALL")
                        .status("Message sent to queue")
                        .build()
                )
                .productList(productList)
                .build();

    }

    @Override
    public Response getProductsByCategory(Long categoryId) {
        List<Product> products = productRepo.findByCategoryId(categoryId);
        if(products.isEmpty()){
            throw new NotFoundException("No Products found for this category");
        }
        List<ProductDto> productDtoList = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("Product retrieved by category successfully")
                .queueMessage(QueueMessage.builder()
                        .action("FIND_BY_CATEGORY")
                        .status("Message sent to queue")
                        .build()
                )
                .productList(productDtoList)
                .build();

    }

    @Override
    public Response searchProduct(String searchValue) {
        List<Product> products = productRepo.findByNameContainingOrDescriptionContaining(searchValue, searchValue);

        if (products.isEmpty()){
            throw new NotFoundException("No Products Found");
        }
        List<ProductDto> productDtoList = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());


        return Response.builder()
                .status(200)
                .message("Search product successful")
                .queueMessage(QueueMessage.builder()
                        .action("SEARCH_PRODUCT")
                        .status("Message sent to queue")
                        .build()
                )
                .productList(productDtoList)
                .build();
    }

    @Override
    public Product fetchProductById(Long productId) {
        return productRepo.fetchProductById(productId);
    }

    @Override
    public Response findByBrandName(String brandName) {
        List<Product> products = productRepo.findByBrandName(brandName);
        if (products.isEmpty()) {
            throw new NotFoundException("No products found for brand: " + brandName);
        }
        List<ProductDto> productDtoList = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .message("Products retrieved by brand name successfully")
                .productList(productDtoList)
                .build();
    }
    }

