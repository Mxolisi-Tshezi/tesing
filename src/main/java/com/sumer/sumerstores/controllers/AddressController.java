package com.sumer.sumerstores.controllers;


import com.sumer.sumerstores.dto.AddressRequest;
import com.sumer.sumerstores.dto.AddressResponse;
import com.sumer.sumerstores.entities.Address;
import com.sumer.sumerstores.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/address")
@CrossOrigin
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/create")
    public ResponseEntity<AddressResponse> createAddress(@RequestBody AddressRequest addressRequest, Principal principal) {
        AddressResponse addressResponse = addressService.createAddress(addressRequest, principal);
        return new ResponseEntity<>(addressResponse, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id){
        addressService.deleteAddress(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
