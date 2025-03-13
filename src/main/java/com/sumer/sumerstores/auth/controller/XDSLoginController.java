package com.sumer.sumerstores.auth.controller;

import com.sumer.sumerstores.services.XDSLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class XDSLoginController {

    private final XDSLoginService xdsLoginService;

    @Autowired
    public XDSLoginController(XDSLoginService xdsLoginService) {
        this.xdsLoginService = xdsLoginService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody Map<String, String> loginDetails) {
        String username = loginDetails.get("username");
        String password = loginDetails.get("password");

        try {
            String connectTicket = xdsLoginService.getAuthenticationTicket(username, password);
            return ResponseEntity.ok(connectTicket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }
}
