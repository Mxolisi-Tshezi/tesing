package com.sumer.sumerstores.repositories;


import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
