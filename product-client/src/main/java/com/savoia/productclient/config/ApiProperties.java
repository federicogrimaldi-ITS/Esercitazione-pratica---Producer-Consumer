package com.savoia.productclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurazione della Producer API ({@code api.base-url}).
 */
@ConfigurationProperties("api")
public record ApiProperties(String baseUrl) {
}
