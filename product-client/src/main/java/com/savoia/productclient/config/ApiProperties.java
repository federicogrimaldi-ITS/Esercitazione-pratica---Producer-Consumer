package com.savoia.productclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configurazione della Producer API ({@code api.base-url}).
 */
@ConfigurationProperties("api")
public record ApiProperties(String baseUrl, List<String> categories, String username, String password) {
}
