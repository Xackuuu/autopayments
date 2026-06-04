package com.bank.autopay.exception;

public class RuleNotFoundException extends RuntimeException {

    public RuleNotFoundException(Long id) {
        super("Rule by id " + id + " not found");
    }
}
