package com.nnk.springboot.exceptions;

import com.nnk.springboot.constants.ErrorCodes;

public class DataPersistException extends BusinessException {


    public DataPersistException(String operationDescription, Throwable cause) {
        super(generateMessage(operationDescription), ErrorCodes.DATA_PERSIST_ERROR, cause);
    }

    private static String generateMessage(String operationDescription) {
        return String.format("Technical error during: %s", operationDescription);
    }
}
