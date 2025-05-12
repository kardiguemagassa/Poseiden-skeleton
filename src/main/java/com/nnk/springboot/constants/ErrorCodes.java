package com.nnk.springboot.constants;

public final class ErrorCodes {

    private ErrorCodes() {}

    public static final String NOT_FOUND = "NOT_FOUND";                 // 404
    public static final String DATA_ACCESS_ERROR = "DATA_ACCESS_ERROR";                 // 404
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";       // 409
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";             // 401
    public static final String PASSWORD_MISMATCH = "PASSWORD_MISMATCH";           // 400
    public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";       // 403

    public static final String INVALID_INPUT = "INVALID_INPUT";                   // 400
    public static final String MISSING_REQUIRED_FIELD = "MISSING_REQUIRED_FIELD"; // 400

    public static final String ACCESS_DENIED = "ACCESS_DENIED";                   // 403
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";   // 401
    public static final String SESSION_EXPIRED = "SESSION_EXPIRED";               // 401

    public static final String DATABASE_ERROR = "DATABASE_ERROR";                 // 500
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";   // 500
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";       // 503

    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";                   // 520 (non standard, pour erreurs inconnues)

    public static final String DATA_PERSIST_ERROR = "DATA_PERSIST_ERROR";
    public static final String INVALID_USER_DATA = "INVALID_USER_DATA";
}
