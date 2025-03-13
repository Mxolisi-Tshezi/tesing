package com.sumer.sumerstores.auth.controller;
import com.sumer.sumerstores.dto.OzowPaymentDto;
import com.sumer.sumerstores.services.OzowPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ozow")
public class OzowPaymentController {

    private final OzowPaymentService ozowPaymentService;

    @Autowired
    public OzowPaymentController(OzowPaymentService ozowPaymentService) {
        this.ozowPaymentService = ozowPaymentService;
    }

    @PostMapping("/initiate-payment")
    public ResponseEntity<String> initiatePayment(@RequestBody OzowPaymentDto paymentDto) {
        try {
            // Call the initiateOzowPayment method of the service
            String response = ozowPaymentService.initiateOzowPayment(paymentDto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Return a custom error message if something goes wrong
            return new ResponseEntity<>("Error initiating Ozow payment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

