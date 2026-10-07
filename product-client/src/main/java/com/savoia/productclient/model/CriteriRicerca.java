package com.savoia.productclient.model;

/**
 * Criteri di ricerca dei prodotti (value object immutabile).
 * I valori vuoti o composti solo da spazi sono considerati assenti;
 * la categoria è espressa con il codice dell'enum della Producer (es. {@code INFORMATICA}).
 */
public record CriteriRicerca(String nome, String categoria) {

    public CriteriRicerca {
        nome = normalizza(nome);
        categoria = normalizza(categoria) == null ? null : new Categoria(categoria).codice();
    }

    public static CriteriRicerca nessuno() {
        return new CriteriRicerca(null, null);
    }

    public boolean isVuoto() {
        return nome == null && categoria == null;
    }

    private static String normalizza(String valore) {
        return valore == null || valore.isBlank() ? null : valore.strip();
    }
}
