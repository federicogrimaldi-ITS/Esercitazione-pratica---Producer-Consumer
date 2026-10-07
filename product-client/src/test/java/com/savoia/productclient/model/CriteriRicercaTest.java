package com.savoia.productclient.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CriteriRicercaTest {

    @Test
    void stringheVuoteOSoloSpaziSonoConsiderateAssenti() {
        CriteriRicerca criteri = new CriteriRicerca("   ", "");

        assertThat(criteri.nome()).isNull();
        assertThat(criteri.categoria()).isNull();
        assertThat(criteri.isVuoto()).isTrue();
    }

    @Test
    void iValoriVengonoRipulitiDagliSpazi() {
        CriteriRicerca criteri = new CriteriRicerca("  laptop ", " Informatica ");

        assertThat(criteri.nome()).isEqualTo("laptop");
        assertThat(criteri.categoria()).isEqualTo("Informatica");
        assertThat(criteri.isVuoto()).isFalse();
    }

    @Test
    void categoryIsSentExactlyAsTheProducerEnumValue() {
        assertThat(new CriteriRicerca(null, "Informatica").categoria()).isEqualTo("Informatica");
    }

    @Test
    void nessunoNonHaFiltri() {
        assertThat(CriteriRicerca.nessuno().isVuoto()).isTrue();
    }

    @Test
    void dueCriteriConGliStessiValoriSonoUguali() {
        assertThat(new CriteriRicerca(" laptop", null))
                .isEqualTo(new CriteriRicerca("laptop", ""));
    }
}
