package com.codewithsam.mailautomator.exception;

public class GoogleSheetReadException extends RuntimeException {

    public GoogleSheetReadException(String message) {
        super(message);
    }

    public GoogleSheetReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
