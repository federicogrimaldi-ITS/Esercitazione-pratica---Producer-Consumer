package com.savoia.productclient.exception;

import java.util.Map;

public class InvalidProductException extends ApiErroreException {

    public InvalidProductException(String message, Map<String, String> fieldErrors) {
        super(400, message);
    }

    public Map<String, String> getFieldErrors() {
        return Map.of();
    }
}
