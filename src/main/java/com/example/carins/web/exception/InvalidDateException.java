package com.example.carins.web.exception;

public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String claimDate) {
        super("Invalid claimDate provided ("+ claimDate +"): must be ISO format (YYYY-MM-DD)");
    }

    public InvalidDateException(String customMessage, String claimDate) {
        super(customMessage+claimDate);
    }
}
