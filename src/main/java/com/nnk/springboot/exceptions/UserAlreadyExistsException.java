package com.nnk.springboot.exceptions;

import com.nnk.springboot.constants.ErrorCodes;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String username) {
        super("User '" + username + "' already exists", ErrorCodes.USER_ALREADY_EXISTS);
    }

}
