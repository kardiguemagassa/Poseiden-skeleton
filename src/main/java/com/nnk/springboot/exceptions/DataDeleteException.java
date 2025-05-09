package com.nnk.springboot.exceptions;

import com.nnk.springboot.constants.ErrorCodes;

public class DataDeleteException extends BusinessException {

    private final String errorDeletingUser;

    public DataDeleteException(String errorDeletingUser, Exception e) {
        super(generateMessage(errorDeletingUser), ErrorCodes.NOT_FOUND);
        this.errorDeletingUser = errorDeletingUser;
    }

    private static String generateMessage(String message) {
        return String.format("User not found with ID: %s", message);
    }
    public String getErrorDeletingUser() {
        return errorDeletingUser;
    }
}
