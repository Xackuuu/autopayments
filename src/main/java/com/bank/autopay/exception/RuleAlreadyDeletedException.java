package com.bank.autopay.exception;

public class RuleAlreadyDeletedException extends RuntimeException {

    public RuleAlreadyDeletedException(Long id) {
        super("Rule by id " + id + " already deleted");
    }
}
