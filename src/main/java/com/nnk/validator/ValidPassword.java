package com.nnk.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation to validate the strength of a password.
 *
 * <p>This constraint applies the rules defined in {@link PasswordValidator}:</p>
 * <ul>
 *  <li>Minimum 8 characters</li>
 *  <li>At least one uppercase letter</li>
 *  <li>At least one special character (!@#$%^&*)</li>
 *  </ul>
 *  It can be applied to entity fields or methods.
 *
 *  @see PasswordValidator
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    /**
     * Default message displayed if validation fails.
     * @return Error message
     */
    String message() default "Invalid password!!";

    /**
     * Validation groups (not used in this context).
     * @return groups
     */
    Class<?>[] groups() default {};

    /**
     *Payload to provide additional information about the constraint.
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};
}
