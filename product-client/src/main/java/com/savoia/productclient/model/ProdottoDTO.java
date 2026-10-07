package com.savoia.productclient.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modello lato client di un prodotto, costruito dal JSON restituito dalla Producer API.
 * I nomi dei campi JSON sono quelli della Producer (in inglese); la categoria è il codice dell'enum.
 */
public record ProdottoDTO(
        Long id,
        @JsonProperty("name") String nome,
        @JsonProperty("description") String descrizione,
        @JsonProperty("price") BigDecimal prezzo,
        @JsonProperty("category") String categoria,
        @JsonProperty("quantity") Integer quantita,
        @JsonProperty("creationDate") LocalDateTime dataCreazione) {

    public String categoriaLeggibile() {
        return new Categoria(categoria).etichetta();
    }
}
