package com.mybank.front.client;

import java.util.List;

public class CashValidationException extends RuntimeException {

    private final List<String> errors;

    public CashValidationException(List<String> errors) {
        super("Cash operation validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
