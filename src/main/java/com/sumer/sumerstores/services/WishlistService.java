package com.sumer.sumerstores.services;


import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.auth.entities.Wishlist;
import com.sumer.sumerstores.entities.Product;
import com.sumer.sumerstores.exceptions.WishlistNotFoundException;

public interface WishlistService {

    Wishlist createWishlist(User user);

    Wishlist getWishlistByUserId(User user);

    Wishlist addProductToWishlist(User user, Product product) throws WishlistNotFoundException;

}

