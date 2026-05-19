package com.mybank.front.client;

import java.util.List;

public class AccountUpdateValidationException extends RuntimeException {

    private final List<String> errors;

    public AccountUpdateValidationException(List<String> errors) {
        super("Account profile validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
