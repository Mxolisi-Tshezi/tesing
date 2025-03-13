package com.sumer.sumerstores.auth.repositories;


import com.sumer.sumerstores.auth.dto.RegistrationRequest;
import com.sumer.sumerstores.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String username);

    Optional<User> findBySaIdNumber(String saIdNumber);
}
