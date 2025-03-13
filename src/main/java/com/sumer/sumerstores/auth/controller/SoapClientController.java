package com.sumer.sumerstores.auth.controller;
import com.sumer.sumerstores.services.SoapClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/soap")
public class SoapClientController {

    private final SoapClient soapClient;

    public SoapClientController() {
        this.soapClient = new SoapClient();
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        try {
            String response = soapClient.sendSoapLoginRequest(username, password);
            return response;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

