package com.savoia.productclient.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFormTest {

    @Test
    void formIsFilledFromAnExistingProduct() {
        ProdottoDTO laptop = new ProdottoDTO(1L, "Laptop Pro 15", "Notebook", new BigDecimal("1299.90"),
                "Informatica", 15, null);

        assertThat(ProductForm.from(laptop)).isEqualTo(
                new ProductForm("Laptop Pro 15", "Notebook", new BigDecimal("1299.90"), "Informatica", 15));
    }
}
