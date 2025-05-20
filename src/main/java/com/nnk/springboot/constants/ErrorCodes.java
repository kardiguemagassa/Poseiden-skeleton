package com.nnk.springboot.constants;

public final class ErrorCodes {

    private ErrorCodes() {}

    public static final String NOT_FOUND = "NOT_FOUND";                 // 404
    public static final String DATA_ACCESS_ERROR = "DATA_ACCESS_ERROR";                 // 404
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";       // 409
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";             // 401
    public static final String DATA_PERSIST_ERROR = "DATA_PERSIST_ERROR";
    public static final String INVALID_USER_DATA = "INVALID_USER_DATA";
}
