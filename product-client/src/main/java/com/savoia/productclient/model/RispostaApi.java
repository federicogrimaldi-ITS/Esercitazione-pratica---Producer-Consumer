package com.savoia.productclient.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Busta JSON ({@code ResponseApi}) con cui la Producer restituisce gli errori:
 * il testo da mostrare è in {@code message}, i dettagli di validazione in {@code errors}.
 */
public record RispostaApi<T>(
        LocalDateTime timestamp,
        String httpStatus,
        String error,
        String message,
        Map<String, String> errors,
        T data) {
}
