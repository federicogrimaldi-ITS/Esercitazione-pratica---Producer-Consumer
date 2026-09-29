package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Client HTTP verso la Producer API: unico punto dell'applicazione che conosce gli endpoint REST.
 */
@Component
public class ProdottoApiClient {

    private static final ParameterizedTypeReference<List<ProdottoDTO>> LISTA_PRODOTTI =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public ProdottoApiClient(RestClient.Builder builder, ApiProperties apiProperties) {
        this.restClient = builder.baseUrl(apiProperties.baseUrl()).build();
    }

    public List<ProdottoDTO> trovaTutti(CriteriRicerca criteri) {
        return restClient.get()
                .uri(uri -> uri.path("/prodotti")
                        .queryParamIfPresent("nome", Optional.ofNullable(criteri.nome()))
                        .queryParamIfPresent("categoria", Optional.ofNullable(criteri.categoria()))
                        .build())
                .retrieve()
                .body(LISTA_PRODOTTI);
    }

    public ProdottoDTO trovaPerId(Long id) {
        return restClient.get()
                .uri("/prodotti/{id}", id)
                .retrieve()
                .body(ProdottoDTO.class);
    }
}
