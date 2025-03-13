package com.sumer.sumerstores.auth.controller;


import com.sumer.sumerstores.auth.config.JWTTokenHelper;
import com.sumer.sumerstores.auth.dto.LoginRequest;
import com.sumer.sumerstores.auth.dto.RegistrationRequest;
import com.sumer.sumerstores.auth.dto.RegistrationResponse;
import com.sumer.sumerstores.auth.dto.UserToken;
import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.auth.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RegistrationService registrationService;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    JWTTokenHelper jwtTokenHelper;


    @PostMapping("/login")
    public ResponseEntity<UserToken> login(@RequestBody LoginRequest loginRequest){
        try{
            Authentication authentication= UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getUserName(),
                    loginRequest.getPassword());

            Authentication authenticationResponse = this.authenticationManager.authenticate(authentication);

            if(authenticationResponse.isAuthenticated()){
                User user= (User) authenticationResponse.getPrincipal();
                if(!user.isEnabled()) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }

                String token =jwtTokenHelper.generateToken(user.getEmail());
                UserToken userToken= UserToken.builder().token(token).build();
                return new ResponseEntity<>(userToken,HttpStatus.OK);
            }

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request){
        RegistrationResponse registrationResponse = registrationService.createUser(request);

        return new ResponseEntity<>(registrationResponse,
                registrationResponse.getCode() == 200 ? HttpStatus.OK: HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> map) {
        String userName = map.get("userName");
        String code = map.get("code");
        User user = (User) userDetailsService.loadUserByUsername(userName);
        if (user != null) {
            if (user.isEnabled()) {
                String alreadyVerifiedMessage = "This email has already been verified";
                return new ResponseEntity<>(alreadyVerifiedMessage, HttpStatus.BAD_REQUEST);
            }
            if (user.getConfirmationCode().equals(code)) {
                registrationService.verifyUser(userName);
                String successMessage = "Your email has been successfully verified";
                return new ResponseEntity<>(successMessage, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Invalid verification code or user not found", HttpStatus.BAD_REQUEST);
    }


}
