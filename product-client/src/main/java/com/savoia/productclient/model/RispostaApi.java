package com.savoia.productclient.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Busta JSON ({@code ResponseApi}) con cui la Producer avvolge tutte le risposte:
 * il contenuto utile è in {@code data}; per gli errori il testo è in {@code message}
 * e i dettagli di validazione in {@code errors}.
 */
public record RispostaApi<T>(
        LocalDateTime timestamp,
        String httpStatus,
        String error,
        String message,
        Map<String, String> errors,
        T data) {
}
