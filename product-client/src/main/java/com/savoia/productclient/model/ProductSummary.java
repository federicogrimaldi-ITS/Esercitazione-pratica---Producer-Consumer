package com.savoia.productclient.model;

import java.math.BigDecimal;

/**
 * Product as listed by the Producer API (ProductResponse): only the fields shown in the product table.
 */
public record ProductSummary(Long id, String name, BigDecimal price, String category) {

    public String categoryLabel() {
        return new Categoria(category).etichetta();
    }
}
