package com.savoia.productclient.model;

/**
 * Criteri di ricerca dei prodotti (value object immutabile).
 * I valori vuoti o composti solo da spazi sono considerati assenti;
 * la categoria è espressa con il valore dell'enum della Producer (es. {@code Informatica});
 * {@code priceSort} è l'ordinamento per prezzo, assente se non richiesto.
 */
public record CriteriRicerca(String nome, String categoria, PriceSort priceSort) {

    public CriteriRicerca {
        nome = normalizza(nome);
        categoria = normalizza(categoria);
    }

    public CriteriRicerca(String nome, String categoria) {
        this(nome, categoria, null);
    }

    public static CriteriRicerca nessuno() {
        return new CriteriRicerca(null, null);
    }

    public boolean isVuoto() {
        return nome == null && categoria == null && priceSort == null;
    }

    private static String normalizza(String valore) {
        return valore == null || valore.isBlank() ? null : valore.strip();
    }
}
