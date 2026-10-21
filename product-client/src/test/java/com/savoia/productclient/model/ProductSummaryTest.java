package com.savoia.productclient.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSummaryTest {

    @Test
    void categoryLabelIsTheProducerEnumValue() {
        ProductSummary product = new ProductSummary(1L, "Mouse", BigDecimal.TEN, "Accessori");

        assertThat(product.categoryLabel()).isEqualTo("Accessori");
    }

    @Test
    void missingCategoryHasAnEmptyLabel() {
        ProductSummary product = new ProductSummary(1L, "Mouse", BigDecimal.TEN, null);

        assertThat(product.categoryLabel()).isEmpty();
    }
}
