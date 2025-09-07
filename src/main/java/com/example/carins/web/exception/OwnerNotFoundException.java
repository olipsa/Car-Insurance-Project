package com.example.carins.web.exception;


public class OwnerNotFoundException extends RuntimeException {
    public OwnerNotFoundException(Long ownerId) {
        super("Owner with id " + ownerId + " does not exist");
    }
}
