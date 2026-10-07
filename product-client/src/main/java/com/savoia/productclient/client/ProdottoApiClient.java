package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.config.CacheConfig;
import com.savoia.productclient.exception.ApiErroreException;
import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.PriceSort;
import com.savoia.productclient.model.ProductForm;
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

    public ProdottoApiClient(RestClient.Builder builder, ApiProperties apiProperties) {
        this.restClient = builder.baseUrl(apiProperties.baseUrl()).build();
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

    public ProdottoDTO createProduct(ProductForm productForm) {
        throw new UnsupportedOperationException();
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
