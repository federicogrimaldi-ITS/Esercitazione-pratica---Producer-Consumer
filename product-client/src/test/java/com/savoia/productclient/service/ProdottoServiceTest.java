package com.savoia.productclient.service;

import com.savoia.productclient.client.ProdottoApiClient;
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
                new ProdottoDTO(1L, "Laptop Pro 15", null, new BigDecimal("1299.90"), "Informatica", 15, null));
        when(client.trovaTutti(criteri)).thenReturn(prodotti);

        assertThat(service.cercaProdotti(criteri)).isEqualTo(prodotti);
    }
}
