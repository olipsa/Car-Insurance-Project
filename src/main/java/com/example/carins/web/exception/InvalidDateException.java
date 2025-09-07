package com.example.carins.web.exception;

public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String date) {
        super("Invalid date provided ("+ date +"): must be ISO format (YYYY-MM-DD)");
    }

    public InvalidDateException(String customMessage, String date) {
        super(customMessage+date);
    }
}
