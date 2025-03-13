package com.sumer.sumerstores.auth.controller;
import com.sumer.sumerstores.dto.KYCRequestDTO;
import com.sumer.sumerstores.dto.KYCResponseDTO;
import com.sumer.sumerstores.services.KYCService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
public class KYCController {

    private final KYCService kycService;

    public KYCController(KYCService kycService) {
        this.kycService = kycService;
    }

    /**
     * Perform KYC verification based on the provided request details.
     *
     * @param kycRequest The KYC request data.
     * @return The response containing KYC results.
     */
    @PostMapping("/verify")
    public ResponseEntity<KYCResponseDTO> performKYC(@RequestBody KYCRequestDTO kycRequest) {
        try {
            KYCResponseDTO kycResponse = kycService.performKYC(kycRequest);
            return new ResponseEntity<>(kycResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            KYCResponseDTO errorResponse = KYCResponseDTO.builder()
                    .status("ERROR")
                    .message("An error occurred while processing the KYC request.")
                    .code(500)
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

