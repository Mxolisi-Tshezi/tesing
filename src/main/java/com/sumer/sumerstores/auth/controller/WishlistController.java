package com.sumer.sumerstores.auth.controller;

import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.auth.entities.Wishlist;
import com.sumer.sumerstores.entities.Product;
import com.sumer.sumerstores.exceptions.WishlistNotFoundException;
import com.sumer.sumerstores.services.ProductService;
import com.sumer.sumerstores.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Wishlist> getWishlist(@AuthenticationPrincipal User user) {
        Wishlist wishlist = wishlistService.getWishlistByUserId(user);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<Wishlist> toggleProductInWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId
    ) throws WishlistNotFoundException {
        Product product = productService.getProductById(productId).getProduct();
        Wishlist updatedWishlist = wishlistService.addProductToWishlist(user, product);
        return ResponseEntity.ok(updatedWishlist);
    }
}