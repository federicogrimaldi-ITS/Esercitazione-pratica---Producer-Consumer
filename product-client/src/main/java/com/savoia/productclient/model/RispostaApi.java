package com.savoia.productclient.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Busta JSON ({@code ResponseApi}) con cui la Producer avvolge tutte le risposte:
 * il contenuto utile è in {@code data}, gli errori in {@code message} ed {@code errors}.
 */
public record RispostaApi<T>(
        LocalDateTime timestamp,
        String httpStatus,
        String error,
        String message,
        Map<String, String> errors,
        T data) {
}
