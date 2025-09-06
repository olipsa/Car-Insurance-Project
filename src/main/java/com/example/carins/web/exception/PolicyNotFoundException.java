package com.example.carins.web.exception;


public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException(Long policyId) {
        super("Policy with id " + policyId + " does not exist");
    }
}