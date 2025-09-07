package com.example.carins.web.exception;

public class InvalidCarVinException extends RuntimeException {
    public InvalidCarVinException(String vin) {
        super("Invalid VIN provided ("+ vin +"): car with this VIN already exists");
    }
}
