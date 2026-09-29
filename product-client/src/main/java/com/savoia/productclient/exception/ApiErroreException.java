package com.savoia.productclient.exception;

/**
 * Risposta di errore (diversa da 404) ricevuta dalla Producer API.
 */
public class ApiErroreException extends RuntimeException {

    private final int status;

    public ApiErroreException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
