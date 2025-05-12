package com.nnk.springboot.exceptions;

import static com.nnk.springboot.constants.ErrorCodes.NOT_FOUND;

public class NotFoundException extends BusinessException {

    public NotFoundException(String entityName, Object identifier) {
        super(entityName + "not found identifier: " + identifier, NOT_FOUND);
    }
}
