package com.nnk.springboot.exceptions;


import static com.nnk.springboot.constants.ErrorCodes.ALREADY_EXISTS;

public class AlreadyExistsException extends BusinessException {

    public AlreadyExistsException(String entityName) {
        super(entityName + " already exists: ", ALREADY_EXISTS);
    }

}
