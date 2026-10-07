package com.savoia.productclient.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdottoDTOTest {

    @Test
    void categoriaLeggibileTrasformaIlCodiceEnum() {
        ProdottoDTO prodotto = new ProdottoDTO(1L, "Mouse", null, BigDecimal.TEN, "Accessori", 1, null);

        assertThat(prodotto.categoriaLeggibile()).isEqualTo("Accessori");
    }

    @Test
    void categoriaAssenteRestaVuota() {
        ProdottoDTO prodotto = new ProdottoDTO(1L, "Mouse", null, BigDecimal.TEN, null, 1, null);

        assertThat(prodotto.categoriaLeggibile()).isEmpty();
    }
}
