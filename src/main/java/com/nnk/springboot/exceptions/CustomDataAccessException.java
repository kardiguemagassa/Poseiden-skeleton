package com.nnk.springboot.exceptions;

import com.nnk.springboot.constants.ErrorCodes;

public class CustomDataAccessException extends BusinessException{

    public CustomDataAccessException(String message, Throwable cause) {
        super(message, ErrorCodes.DATA_ACCESS_ERROR, cause);
    }
}
