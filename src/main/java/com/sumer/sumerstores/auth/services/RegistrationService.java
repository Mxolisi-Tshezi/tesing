package com.sumer.sumerstores.auth.services;
import com.sumer.sumerstores.auth.dto.QueueMessage;
import com.sumer.sumerstores.auth.dto.RegistrationRequest;
import com.sumer.sumerstores.auth.dto.RegistrationResponse;
import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.auth.helper.VerificationCodeGenerator;
import com.sumer.sumerstores.auth.repositories.UserDetailRepository;
import com.sumer.sumerstores.auth.repositories.UserRepository;
import com.sumer.sumerstores.services.QueueService;
import com.sumer.sumerstores.services.SanctionsProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final UserDetailRepository userDetailRepository;
    private final AuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final QueueService queueService;
    private final UserRepository userRepository;
    private final SanctionsProcessingService sanctionsProcessingService;

    public RegistrationResponse createUser(RegistrationRequest request) {
        Optional<User> existingUserOpt = userDetailRepository.findByEmail(request.getEmail());
        if (existingUserOpt.isPresent()) {
            log.warn("User registration failed: Email already exists for {}", request.getEmail());
            return RegistrationResponse.builder()
                    .code(400)
                    .message("Email already exists!")
                    .build();
        }

        if (request.getFirstName() == null || request.getLastName() == null || request.getFirstName().isEmpty() || request.getLastName().isEmpty()) {
            log.warn("User registration failed: Full name is required.");
            return RegistrationResponse.builder()
                    .code(400)
                    .message("First name and Last name required.")
                    .build();
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setSaIdNumber(request.getSaIdNumber());
        user.setTitle(request.getTitle());
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        String code = VerificationCodeGenerator.generateCode();
        user.setRole(User.Role.USER);
        user.setConfirmationCode(code);
        user.setAuthorities(authorityService.getUserAuthority());
        log.info("Starting user registration process for email: {}", request.getEmail());
        boolean isSanctioned = sanctionsProcessingService.isUserSanctioned(request.getFirstName(), request.getLastName());
        if (isSanctioned) {
            log.info("User is sanctioned: {} {}", request.getFirstName(), request.getLastName());
            user.setSanctioned(true);
            userDetailRepository.save(user);
            emailService.sendSanctionedNotification(user);
            return RegistrationResponse.builder()
                    .code(200)
                    .message("User registered successfully! However, you are sanctioned and cannot purchase insurance.")
                    .user(user)
                    .build();
        }

        Optional<User> existingSaIdOpt = userDetailRepository.findBySaIdNumber(request.getSaIdNumber());
        if (existingSaIdOpt.isPresent()) {
            log.warn("User registration failed: South African ID number already exists for {}", request.getSaIdNumber());
            return RegistrationResponse.builder()
                    .code(400)
                    .message("South African ID number already exists!")
                    .build();
        }

        if (request.getSaIdNumber() != null && !request.getSaIdNumber().isEmpty()) {
            log.debug("Validating South African ID number: {}", request.getSaIdNumber());
            if (!validateSouthAfricanID(request.getSaIdNumber())) {
                log.warn("Invalid South African ID number: {}", request.getSaIdNumber());
                return RegistrationResponse.builder()
                        .code(400)
                        .message("Invalid South African ID number.")
                        .build();
            }
        }

        log.info("Saving new user with email: {}", request.getEmail());
        userDetailRepository.save(user);

        log.info("Sending confirmation email to user with email: {}", request.getEmail());
        emailService.sendMail(user);
        queueService.sendMessageToQueue(user, "USER_REGISTERED");

        return RegistrationResponse.builder()
                .code(200)
                .message("User created successfully! Please verify your email.")
                .user(user)
                .queueMessage(QueueMessage.builder()
                        .action("USER_REGISTERED")
                        .status("Message sent to queue")
                        .build()
                )
                .build();
    }

    public boolean validateSouthAfricanID(String idNumber) {
        log.debug("Validating South African ID number: {}", idNumber);

        if (idNumber == null || idNumber.isEmpty()) {
            log.error("Validation failed: South African ID number is missing");
            return false;
        }

        if (idNumber.length() != 13) {
            log.error("Validation failed: South African ID number must be 13 digits");
            return false;
        }

        String birthdate = idNumber.substring(0, 6);
        if (!isValidBirthdate(birthdate)) {
            log.error("Validation failed: Invalid birthdate in South African ID number {}", idNumber);
            return false;
        }

        int genderDigits = Integer.parseInt(idNumber.substring(6, 10));
        if (genderDigits < 0 || genderDigits > 9999) {
            log.error("Validation failed: Invalid gender digits in South African ID number {}", idNumber);
            return false;
        }

        if (!validateLuhn(idNumber)) {
            log.error("Validation failed: South African ID number failed Luhn check");
            return false;
        }

        log.info("South African ID number validated successfully");
        return true;
    }

    private boolean isValidBirthdate(String birthdate) {
        try {
            int year = Integer.parseInt(birthdate.substring(0, 2));
            int month = Integer.parseInt(birthdate.substring(2, 4));
            int day = Integer.parseInt(birthdate.substring(4, 6));

            if (month < 1 || month > 12 || day < 1 || day > 31) {
                log.error("Validation failed: Invalid birthdate format in South African ID number");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            log.error("Validation failed: Invalid birthdate format in South African ID number");
            return false;
        }
    }

    private boolean validateLuhn(String idNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = idNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(idNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
    public void verifyUser(String userName) {
        User user = userRepository.findByEmail(userName);
        user.setEnabled(true);
        userDetailRepository.save(user);
    }
}
