package com.savoia.productclient.model;

import java.util.Locale;

/**
 * Categoria di un prodotto (value object).
 * Il codice coincide con il valore dell'enum {@code Categoria} della Producer (es. {@code INFORMATICA});
 * l'etichetta è la forma mostrata all'utente (es. "Informatica").
 */
public record Categoria(String codice) {

    public Categoria {
        codice = codice == null ? null : codice.strip().toUpperCase(Locale.ROOT);
    }

    public String etichetta() {
        if (codice == null || codice.isEmpty()) {
            return "";
        }
        return codice.charAt(0) + codice.substring(1).toLowerCase(Locale.ROOT);
    }
}
