package com.savoia.productclient.service;

import com.savoia.productclient.client.ProdottoApiClient;
import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.model.Categoria;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProdottoServiceTest {

    ProdottoApiClient client = mock(ProdottoApiClient.class);
    ApiProperties apiProperties = new ApiProperties(
            "http://producer.test/api", List.of("Accessori", "Audio", "Informatica"));
    ProdottoService service = new ProdottoService(client, apiProperties);

    @Test
    void cercaProdottiRestituisceIProdottiDellApi() {
        CriteriRicerca criteri = new CriteriRicerca("laptop", null);
        List<ProdottoDTO> prodotti = List.of(
                new ProdottoDTO(1L, "Laptop Pro 15", null, new BigDecimal("1299.90"), "Informatica", 15, null));
        when(client.trovaTutti(criteri)).thenReturn(prodotti);

        assertThat(service.cercaProdotti(criteri)).isEqualTo(prodotti);
    }

    @Test
    void trovaProdottoRestituisceIlDettaglioDellApi() {
        ProdottoDTO laptop = new ProdottoDTO(1L, "Laptop Pro 15", null, new BigDecimal("1299.90"), "Informatica", 15, null);
        when(client.trovaPerId(1L)).thenReturn(laptop);

        assertThat(service.trovaProdotto(1L)).isEqualTo(laptop);
    }

    @Test
    void categoriesComeFromTheConfiguredProducerEnumWithoutCallingTheApi() {
        assertThat(service.categorie()).containsExactly(
                new Categoria("Accessori"), new Categoria("Audio"), new Categoria("Informatica"));
        verifyNoInteractions(client);
    }
}
