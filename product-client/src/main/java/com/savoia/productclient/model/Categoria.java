package com.savoia.productclient.model;

/**
 * Categoria di un prodotto (value object).
 * Il codice coincide con il valore dell'enum {@code Category} della Producer (es. {@code Informatica})
 * ed è anche l'etichetta mostrata all'utente.
 */
public record Categoria(String codice) {

    public Categoria {
        codice = codice == null ? null : codice.strip();
    }

    public String etichetta() {
        return codice == null ? "" : codice;
    }
}
