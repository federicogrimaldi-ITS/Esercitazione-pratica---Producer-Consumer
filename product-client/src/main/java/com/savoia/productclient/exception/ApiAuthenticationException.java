package com.savoia.productclient.exception;

public class ApiAuthenticationException extends ApiErroreException {

    public ApiAuthenticationException(int status) {
        super(status, "The Producer rejected the service credentials");
    }
}
