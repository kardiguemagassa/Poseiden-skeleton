package com.nnk.springboot.validation;

import com.nnk.validator.PasswordValidator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PasswordValidatorTest {

    private PasswordValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
    }

    @Test
    void testPasswordIsNull() {
        boolean result = validator.isValid(null, context);
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password cannot be empty");
    }

    @Test
    void testPasswordIsBlank() {
        boolean result = validator.isValid("   ", context);
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password cannot be empty");
    }

    @Test
    void testPasswordTooShort() {
        boolean result = validator.isValid("A!a1", context);
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("8 characters minimum required");
    }

    @Test
    void testPasswordNoUppercase() {
        boolean result = validator.isValid("password!", context);
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("At least one capital letter required");
    }

    @Test
    void testPasswordNoSpecialCharacter() {
        boolean result = validator.isValid("Password123", context);
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("At least one special character (!@#$%^&*) required");
    }

}
