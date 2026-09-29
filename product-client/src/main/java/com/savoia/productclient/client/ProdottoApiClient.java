package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.config.CacheConfig;
import com.savoia.productclient.exception.ApiErroreException;
import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import com.savoia.productclient.model.RispostaApi;
import org.springframework.cache.annotation.Cacheable;
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
 * Le risposte corrette sono memorizzate nella cache locale fino alla scadenza; gli errori no.
 */
@Component
public class ProdottoApiClient {

    private static final ParameterizedTypeReference<RispostaApi<List<ProdottoDTO>>> LISTA_PRODOTTI =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<RispostaApi<ProdottoDTO>> PRODOTTO =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public ProdottoApiClient(RestClient.Builder builder, ApiProperties apiProperties) {
        this.restClient = builder.baseUrl(apiProperties.baseUrl()).build();
    }

    @Cacheable(CacheConfig.CACHE_PRODOTTI)
    public List<ProdottoDTO> trovaTutti(CriteriRicerca criteri) {
        return dati(esegui(() -> restClient.get()
                .uri(uri -> uri.path("/products")
                        .queryParamIfPresent("nome", Optional.ofNullable(criteri.nome()))
                        .queryParamIfPresent("categoria", Optional.ofNullable(criteri.categoria()))
                        .build())
                .retrieve()
                .body(LISTA_PRODOTTI)));
    }

    @Cacheable(CacheConfig.CACHE_PRODOTTO)
    public ProdottoDTO trovaPerId(Long id) {
        return dati(esegui(() -> restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(PRODOTTO)));
    }

    private static <T> T dati(RispostaApi<T> risposta) {
        return risposta == null ? null : risposta.data();
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
            RispostaApi<?> errore = ex.getResponseBodyAs(RispostaApi.class);
            if (errore != null && errore.message() != null) {
                return errore.message();
            }
        } catch (RestClientException | IllegalStateException ignored) {
            // corpo assente o non in formato JSON: si usa il messaggio generico
        }
        return ex.getStatusText();
    }
}
