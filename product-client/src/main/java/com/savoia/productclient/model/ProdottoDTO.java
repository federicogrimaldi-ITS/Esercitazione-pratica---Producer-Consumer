package com.savoia.productclient.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modello lato client di un prodotto, costruito dal JSON restituito dalla Producer API.
 */
public record ProdottoDTO(
        Long id,
        String nome,
        String descrizione,
        BigDecimal prezzo,
        String categoria,
        Integer quantita,
        LocalDateTime dataCreazione) {
}
