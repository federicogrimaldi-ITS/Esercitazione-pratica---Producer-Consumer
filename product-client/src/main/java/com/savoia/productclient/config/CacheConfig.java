package com.savoia.productclient.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Cache locale in memoria (Caffeine) delle risposte della Producer API.
 * Nomi delle cache e scadenza sono configurati in application.yaml (spring.cache.*).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PRODOTTI = "prodotti";
    public static final String CACHE_PRODOTTO = "prodotto";
}
