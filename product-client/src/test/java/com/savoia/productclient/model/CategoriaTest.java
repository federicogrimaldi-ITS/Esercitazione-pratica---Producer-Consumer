package com.savoia.productclient.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaTest {

    @Test
    void codeKeepsTheProducerEnumValueTrimmed() {
        assertThat(new Categoria(" Informatica ").codice()).isEqualTo("Informatica");
    }

    @Test
    void lEtichettaEPensataPerLUtente() {
        assertThat(new Categoria("Informatica").etichetta()).isEqualTo("Informatica");
    }
}
