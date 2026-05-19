package com.mybank.front.client;

import java.util.List;

public record ApiErrorView(
        String message,
        List<String> errors
) {
}
