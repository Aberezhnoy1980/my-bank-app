package com.mybank.front.client;

import java.util.List;

public class TransferValidationException extends RuntimeException {

    private final List<String> errors;

    public TransferValidationException(List<String> errors) {
        super("Transfer validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
