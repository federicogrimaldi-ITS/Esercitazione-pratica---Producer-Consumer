package com.savoia.productclient.exception;

import java.util.Map;

public class InvalidProductException extends ApiErroreException {

    private final Map<String, String> fieldErrors;

    public InvalidProductException(String message, Map<String, String> fieldErrors) {
        super(400, message);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
