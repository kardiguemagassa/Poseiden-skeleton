package com.nnk.springboot.exceptions;

import static com.nnk.springboot.constants.ErrorCodes.INVALID_PASSWORD;

public class InvalidPasswordException extends BusinessException{
    public InvalidPasswordException(String requirements) {
        super("Invalid Password: " + requirements, INVALID_PASSWORD);
    }
}
