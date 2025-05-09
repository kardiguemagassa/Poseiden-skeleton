package com.nnk.springboot.exceptions;

import com.nnk.springboot.constants.ErrorCodes;

public class InvalidUserException extends BusinessException {

    private final String field;

    public InvalidUserException(String field, String requirement) {
        super(String.format("Validation error - %s: %s", field, requirement), ErrorCodes.INVALID_USER_DATA);
        this.field = field;
    }

    public String getField() {
        return field;
    }

}
