package com.sumer.sumerstores.services;

import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.dto.AddressRequest;
import com.sumer.sumerstores.dto.AddressResponse;
import com.sumer.sumerstores.entities.Address;
import com.sumer.sumerstores.repositories.AddressRepository;
import com.sumer.sumerstores.services.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

import java.security.Principal;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private QueueService queueService;  // Inject QueueService

    public AddressResponse createAddress(AddressRequest addressRequest, Principal principal) {
        // Check if the address already exists for the user (optional validation)
        User user = (User) userDetailsService.loadUserByUsername(principal.getName());
        Address existingAddress = addressRepository.findByUserAndStreetAndCity(user, addressRequest.getStreet(), addressRequest.getProvince());
        if (existingAddress != null) {
            return AddressResponse.builder()
                    .code(400)
                    .message("Address already exists!")
                    .build();
        }
        if (addressRequest.getCity() == null || addressRequest.getCity().isEmpty()) {
            return AddressResponse.builder()
                    .code(400)
                    .message("City is required!")
                    .build();
        }


        try {
            // Load the user from the Principal
            User anotherUser = (User) userDetailsService.loadUserByUsername(principal.getName());

            // Create a new address
            Address address = Address.builder()
                    .id(addressRequest.getAddressId())
                    .name(addressRequest.getName())
                    .street(addressRequest.getStreet())
                    .city(addressRequest.getCity())
                    .province(addressRequest.getProvince())
                    .zipCode(addressRequest.getZipCode())
                    .phoneNumber(addressRequest.getPhoneNumber())
                    .user(user)
                    .build();

            // Save the address in the repository
            Address savedAddress = addressRepository.save(address);

            // Send address creation event to the queue
            queueService.sendMessageToQueue("AddressCreated", savedAddress.getId().toString());

            // Return response with details of the created address
            return AddressResponse.builder()
                    .code(200)
                    .message("Address created successfully!")
                    .data(addressRequest)
                    .queueMessage(QueueMessage.builder()
                            .action("AddressCreated")
                            .status("Message sent to queue")
                            .build())
                    .build();

        } catch (Exception e) {
            // Handle any exceptions and return error response
            System.out.println(e.getMessage());
            throw new ServerErrorException(e.getMessage(), e.getCause());
        }
    }

    public AddressResponse deleteAddress(Long id) {
        try {
            // Check if address exists before deletion
            Address address = addressRepository.findById(id).orElse(null);
            if (address == null) {
                return AddressResponse.builder()
                        .code(404)
                        .message("Address not found!")
                        .build();
            }

            // Delete the address from the repository
            addressRepository.deleteById(id);

            // Send address deletion event to the queue
            queueService.sendMessageToQueue("AddressDeleted", id.toString());

            // Return success response
            return AddressResponse.builder()
                    .code(200)
                    .message("Address deleted successfully!")
                    .queueMessage(QueueMessage.builder()
                            .action("AddressDeleted")
                            .status("Message sent to queue")
                            .build())
                    .build();

        } catch (Exception e) {
            // Handle any exceptions and return error response
            System.out.println(e.getMessage());
            throw new ServerErrorException(e.getMessage(), e.getCause());
        }
    }
}
