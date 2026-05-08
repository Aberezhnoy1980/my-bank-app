package com.mybank.cash.api;

import java.util.List;

public record ApiErrorResponse(
        String message,
        List<String> errors
) {
}
