package com.sumer.sumerstores.repositories;


import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    public Address findByUserAndStreetAndCity(User user, String street, String city);

}
