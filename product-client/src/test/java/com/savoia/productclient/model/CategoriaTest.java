package com.savoia.productclient.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaTest {

    @Test
    void ilCodiceEQuelloDellEnumDellaProducer() {
        assertThat(new Categoria(" informatica ").codice()).isEqualTo("INFORMATICA");
    }

    @Test
    void lEtichettaEPensataPerLUtente() {
        assertThat(new Categoria("INFORMATICA").etichetta()).isEqualTo("Informatica");
        assertThat(new Categoria("NETWORKING").etichetta()).isEqualTo("Networking");
    }
}
