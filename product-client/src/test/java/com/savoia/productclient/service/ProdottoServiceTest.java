package com.savoia.productclient.service;

import com.savoia.productclient.client.ProdottoApiClient;
import com.savoia.productclient.model.Categoria;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProdottoServiceTest {

    ProdottoApiClient client = mock(ProdottoApiClient.class);
    ProdottoService service = new ProdottoService(client);

    @Test
    void cercaProdottiRestituisceIProdottiDellApi() {
        CriteriRicerca criteri = new CriteriRicerca("laptop", null);
        List<ProdottoDTO> prodotti = List.of(
                new ProdottoDTO(1L, "Laptop Pro 15", null, new BigDecimal("1299.90"), "INFORMATICA", 15, null));
        when(client.trovaTutti(criteri)).thenReturn(prodotti);

        assertThat(service.cercaProdotti(criteri)).isEqualTo(prodotti);
    }

    @Test
    void trovaProdottoRestituisceIlDettaglioDellApi() {
        ProdottoDTO laptop = new ProdottoDTO(1L, "Laptop Pro 15", null, new BigDecimal("1299.90"), "INFORMATICA", 15, null);
        when(client.trovaPerId(1L)).thenReturn(laptop);

        assertThat(service.trovaProdotto(1L)).isEqualTo(laptop);
    }

    @Test
    void categorieRestituisceLeCategorieDistinteInOrdineAlfabetico() {
        when(client.trovaTutti(CriteriRicerca.nessuno())).thenReturn(List.of(
                prodotto("Mouse", "ACCESSORI"),
                prodotto("Laptop", "INFORMATICA"),
                prodotto("Cuffie", "AUDIO"),
                prodotto("Tastiera", "ACCESSORI")));

        assertThat(service.categorie()).containsExactly(
                new Categoria("ACCESSORI"), new Categoria("AUDIO"), new Categoria("INFORMATICA"));
    }

    private static ProdottoDTO prodotto(String nome, String categoria) {
        return new ProdottoDTO(null, nome, null, BigDecimal.ONE, categoria, 1, null);
    }
}
