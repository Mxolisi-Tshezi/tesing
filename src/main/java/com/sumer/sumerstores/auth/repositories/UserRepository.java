package com.sumer.sumerstores.auth.repositories;

import com.sumer.sumerstores.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String userName);
}
