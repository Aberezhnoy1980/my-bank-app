package com.mybank.cash.client;

import java.util.List;

public record ApiErrorView(
        String message,
        List<String> errors
) {
}
