package com.savoia.productclient.exception;

/**
 * La Producer API non è raggiungibile (servizio spento, timeout, errore di rete).
 */
public class ApiNonDisponibileException extends RuntimeException {

    public ApiNonDisponibileException(Throwable cause) {
        super("Il servizio API non è attualmente disponibile.", cause);
    }
}
