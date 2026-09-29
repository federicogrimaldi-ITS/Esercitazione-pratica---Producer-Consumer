package com.savoia.productclient.model;

import java.time.LocalDateTime;

/**
 * Corpo JSON degli errori restituiti dalla Producer API.
 */
public record ApiErrorDTO(int status, String message, LocalDateTime timestamp) {
}
