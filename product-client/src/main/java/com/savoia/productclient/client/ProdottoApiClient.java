package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.exception.ApiErroreException;
import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.ApiErrorDTO;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Client HTTP verso la Producer API: unico punto dell'applicazione che conosce gli endpoint REST.
 * Gli errori HTTP e di rete vengono tradotti in eccezioni applicative.
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
        return esegui(() -> restClient.get()
                .uri(uri -> uri.path("/prodotti")
                        .queryParamIfPresent("nome", Optional.ofNullable(criteri.nome()))
                        .queryParamIfPresent("categoria", Optional.ofNullable(criteri.categoria()))
                        .build())
                .retrieve()
                .body(LISTA_PRODOTTI));
    }

    public ProdottoDTO trovaPerId(Long id) {
        return esegui(() -> restClient.get()
                .uri("/prodotti/{id}", id)
                .retrieve()
                .body(ProdottoDTO.class));
    }

    private <T> T esegui(Supplier<T> chiamata) {
        try {
            return chiamata.get();
        } catch (RestClientResponseException ex) {
            String messaggio = messaggioDiErrore(ex);
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ProdottoNonTrovatoException(messaggio);
            }
            throw new ApiErroreException(ex.getStatusCode().value(), messaggio);
        } catch (ResourceAccessException ex) {
            throw new ApiNonDisponibileException(ex);
        }
    }

    private String messaggioDiErrore(RestClientResponseException ex) {
        try {
            ApiErrorDTO errore = ex.getResponseBodyAs(ApiErrorDTO.class);
            if (errore != null && errore.message() != null) {
                return errore.message();
            }
        } catch (RestClientException | IllegalStateException ignored) {
            // corpo assente o non in formato JSON: si usa il messaggio generico
        }
        return ex.getStatusText();
    }
}
