package com.sumer.sumerstores.entities;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SaIdNumberValidator implements ConstraintValidator<ValidSaIdNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() != 13) {
            return false;  // Length should be exactly 13
        }

        // Regex to check the format YYMMDDSSSSCAZ
        if (!value.matches("\\d{6}\\d{4}\\d{1}\\d{1}\\d{1}")) {
            return false;
        }

        // Apply checksum validation using the Luhn algorithm
        return validateLuhn(value);
    }

    private boolean validateLuhn(String idNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = idNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(idNumber.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }

            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}

