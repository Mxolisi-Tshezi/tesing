package com.sumer.sumerstores.auth.repositories;


import com.sumer.sumerstores.auth.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Wishlist findByUserId(Long userId);
}
