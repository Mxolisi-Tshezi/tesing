package com.sumer.sumerstores.entities;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Correct reference to the validator using jakarta.validation
@Constraint(validatedBy = SaIdNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaIdNumber {
    String message() default "Invalid South African ID number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
