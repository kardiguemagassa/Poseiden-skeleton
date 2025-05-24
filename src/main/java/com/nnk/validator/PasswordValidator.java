package com.nnk.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom validator for user passwords.
 * <p>
 *     Ce validateur est déclenché par l'annotation {@link ValidPassword}
 *     and allows you to check that the password complies with a security policy:
 * </p>
 * <ul>
 *    <li>Not empty</li>
 *    <li>Minimum 8 characters</li>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one special character (!@#$%^&*)</li>
 * </ul>
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    /**
     * Checks if the password is valid according to the defined security rules.
     *
     * @param password the password to validate
     * @param context the validation context to construct a custom error message
     * @return {@code true} if the password is correct, {@code false} otherwise
     */
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

    /**
     * Constructs a custom violation message in the validation context.
     *
     * @param context the validation context
     * @param message the error message to display
     */
    private void buildConstraint(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
