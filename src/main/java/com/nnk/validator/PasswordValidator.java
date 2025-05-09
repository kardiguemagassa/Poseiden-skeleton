package com.nnk.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;



public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null || password.isBlank()) {
            buildConstraint(context, "Password cannot be empty");
            return false;
        }

        if (password.length() < 8) {
            buildConstraint(context, "8 characters minimum required");
            return false;
        }

        if (password.equals(password.toLowerCase())) {
            buildConstraint(context, "At least one capital letter required");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*].*")) {
            buildConstraint(context, "At least one special character (!@#$%^&*) required");
            return false;
        }

        return true;
    }

    private void buildConstraint(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
