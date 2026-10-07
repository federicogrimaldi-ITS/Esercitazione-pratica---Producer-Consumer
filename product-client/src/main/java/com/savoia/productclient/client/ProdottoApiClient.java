package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.config.CacheConfig;
import com.savoia.productclient.exception.ApiAuthenticationException;
import com.savoia.productclient.exception.ApiErroreException;
import com.savoia.productclient.exception.InvalidProductException;
import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.PriceSort;
import com.savoia.productclient.model.ProductForm;
import com.savoia.productclient.model.ProdottoDTO;
import com.savoia.productclient.model.RispostaApi;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
    private final ApiProperties apiProperties;

    public ProdottoApiClient(RestClient.Builder builder, ApiProperties apiProperties) {
        this.restClient = builder.baseUrl(apiProperties.baseUrl()).build();
        this.apiProperties = apiProperties;
    }

    /**
     * Traduce i criteri negli endpoint della Producer, che non combinano i filtri:
     * <ul>
     *   <li>nessun filtro: {@code GET /products}, oppure {@code GET /products/sort?direction=...}
     *       se è richiesto solo l'ordinamento per prezzo</li>
     *   <li>solo nome: {@code GET /products/search?name=...}</li>
     *   <li>categoria (con o senza nome): {@code GET /products/category/{categoria}};
     *       il nome, se presente, viene applicato qui con lo stesso criterio della Producer
     *       (il nome contiene il testo, senza distinzione tra maiuscole e minuscole)</li>
     * </ul>
     * Con filtri e ordinamento insieme, l'ordinamento per prezzo viene applicato qui.
     */
    @Cacheable(CacheConfig.CACHE_PRODOTTI)
    public List<ProdottoDTO> trovaTutti(CriteriRicerca criteri) {
        boolean hasFilters = criteri.nome() != null || criteri.categoria() != null;
        if (!hasFilters && criteri.priceSort() != null) {
            return findSortedByPrice(criteri.priceSort());
        }
        List<ProdottoDTO> prodotti = filtra(criteri);
        return criteri.priceSort() == null ? prodotti : sortByPrice(prodotti, criteri.priceSort());
    }

    private List<ProdottoDTO> filtra(CriteriRicerca criteri) {
        if (criteri.categoria() != null) {
            List<ProdottoDTO> perCategoria = lista("/products/category/{categoria}", criteri.categoria());
            if (criteri.nome() == null) {
                return perCategoria;
            }
            return perCategoria.stream()
                    .filter(p -> containsIgnoringCase(p.nome(), criteri.nome()))
                    .toList();
        }
        if (criteri.nome() != null) {
            return dati(esegui(() -> restClient.get()
                    .uri(uri -> uri.path("/products/search").queryParam("name", criteri.nome()).build())
                    .retrieve()
                    .body(LISTA_PRODOTTI)));
        }
        return lista("/products");
    }

    private List<ProdottoDTO> findSortedByPrice(PriceSort priceSort) {
        return dati(esegui(() -> restClient.get()
                .uri(uri -> uri.path("/products/sort").queryParam("direction", priceSort.name()).build())
                .retrieve()
                .body(LISTA_PRODOTTI)));
    }

    private static List<ProdottoDTO> sortByPrice(List<ProdottoDTO> prodotti, PriceSort priceSort) {
        Comparator<ProdottoDTO> byPrice = Comparator.comparing(
                ProdottoDTO::prezzo, Comparator.nullsLast(Comparator.naturalOrder()));
        return prodotti.stream()
                .sorted(priceSort == PriceSort.ASC ? byPrice : byPrice.reversed())
                .toList();
    }

    @CacheEvict(cacheNames = {CacheConfig.CACHE_PRODOTTI, CacheConfig.CACHE_PRODOTTO}, allEntries = true)
    public ProdottoDTO createProduct(ProductForm productForm) {
        return dati(esegui(() -> restClient.post()
                .uri("/products")
                .headers(this::authenticate)
                .contentType(MediaType.APPLICATION_JSON)
                .body(productForm)
                .retrieve()
                .body(PRODOTTO)));
    }

    @CacheEvict(cacheNames = {CacheConfig.CACHE_PRODOTTI, CacheConfig.CACHE_PRODOTTO}, allEntries = true)
    public ProdottoDTO updateProduct(Long id, ProductForm productForm) {
        return dati(esegui(() -> restClient.put()
                .uri("/products/{id}", id)
                .headers(this::authenticate)
                .contentType(MediaType.APPLICATION_JSON)
                .body(productForm)
                .retrieve()
                .body(PRODOTTO)));
    }

    @CacheEvict(cacheNames = {CacheConfig.CACHE_PRODOTTI, CacheConfig.CACHE_PRODOTTO}, allEntries = true)
    public void deleteProduct(Long id) {
        esegui(() -> restClient.delete()
                .uri("/products/{id}", id)
                .headers(this::authenticate)
                .retrieve()
                .toBodilessEntity());
    }

    private void authenticate(HttpHeaders headers) {
        headers.setBasicAuth(apiProperties.username(), apiProperties.password());
    }

    /** {@code GET /products/{id}}. */
    @Cacheable(CacheConfig.CACHE_PRODOTTO)
    public ProdottoDTO trovaPerId(Long id) {
        return dati(esegui(() -> restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(PRODOTTO)));
    }

    private static boolean containsIgnoringCase(String text, String part) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(part.toLowerCase(Locale.ROOT));
    }

    private List<ProdottoDTO> lista(String uri, Object... variabili) {
        return dati(esegui(() -> restClient.get()
                .uri(uri, variabili)
                .retrieve()
                .body(LISTA_PRODOTTI)));
    }

    /** Estrae il contenuto utile dalla busta {@code ResponseApi} della Producer. */
    private static <T> T dati(RispostaApi<T> risposta) {
        return risposta == null ? null : risposta.data();
    }

    private <T> T esegui(Supplier<T> chiamata) {
        try {
            return chiamata.get();
        } catch (RestClientResponseException ex) {
            throw traduciErrore(ex);
        } catch (ResourceAccessException ex) {
            throw new ApiNonDisponibileException(ex);
        }
    }

    private static RuntimeException traduciErrore(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            return new ApiAuthenticationException(status);
        }
        RispostaApi<?> errorBody = readErrorBody(ex);
        String messaggio = errorBody != null && errorBody.message() != null ? errorBody.message() : ex.getStatusText();
        if (status == HttpStatus.NOT_FOUND.value()) {
            return new ProdottoNonTrovatoException(messaggio);
        }
        if (status == HttpStatus.BAD_REQUEST.value() && errorBody != null && errorBody.errors() != null) {
            return new InvalidProductException(messaggio, errorBody.errors());
        }
        return new ApiErroreException(status, messaggio);
    }

    private static RispostaApi<?> readErrorBody(RestClientResponseException ex) {
        try {
            return ex.getResponseBodyAs(RispostaApi.class);
        } catch (RestClientException | IllegalStateException notJson) {
            return null;
        }
    }
}
