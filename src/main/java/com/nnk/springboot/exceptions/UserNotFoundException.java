package com.nnk.springboot.exceptions;

import static com.nnk.springboot.constants.ErrorCodes.NOT_FOUND;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Integer id) {
        super("User not found with ID: " +  id, NOT_FOUND);
    }
}
