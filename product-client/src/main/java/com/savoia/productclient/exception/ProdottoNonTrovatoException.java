package com.savoia.productclient.exception;

/**
 * La Producer API ha risposto 404 per il prodotto richiesto.
 */
public class ProdottoNonTrovatoException extends RuntimeException {

    public ProdottoNonTrovatoException(String message) {
        super(message);
    }
}
