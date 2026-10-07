package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.exception.ApiAuthenticationException;
import com.savoia.productclient.exception.ApiErroreException;
import com.savoia.productclient.exception.InvalidProductException;
import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.PriceSort;
import com.savoia.productclient.model.ProductForm;
import com.savoia.productclient.model.ProdottoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@RestClientTest(ProdottoApiClient.class)
@TestPropertySource(properties = {
        "api.base-url=http://producer.test/api",
        "api.username=admin",
        "api.password=secret"})
class ProdottoApiClientTest {

    /** Prodotto come lo serializza la Producer (entity Prodotto, campi in inglese, categoria enum). */
    static final String PRODOTTO_JSON = """
            {
                "id": 1,
                "name": "Laptop Pro 15",
                "description": "Notebook professionale",
                "price": 1299.90,
                "category": "Informatica",
                "quantity": 15,
                "creationDate": "2026-09-01T09:00:00"
            }
            """;

    /** Busta ResponseApi con cui la Producer restituisce ogni risposta corretta (il contenuto è in data). */
    static String risposta(String data) {
        return """
                {"data": %s, "error": null, "errors": null, "httpStatus": "200 OK", "message": null, "timestamp": "2026-09-29T10:00:00.123456789"}
                """.formatted(data);
    }

    @TestConfiguration
    @EnableConfigurationProperties(ApiProperties.class)
    static class Config {
    }

    @Autowired
    ProdottoApiClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void trovaTuttiSenzaFiltriChiamaLaListaCompleta() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andExpect(method(GET))
                .andRespond(withSuccess(risposta("[" + PRODOTTO_JSON + "]"), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(CriteriRicerca.nessuno());

        assertThat(prodotti).extracting(ProdottoDTO::nome).containsExactly("Laptop Pro 15");
        server.verify();
    }

    @Test
    void ricercaPerNomeUsaLEndpointSearch() {
        server.expect(requestTo("http://producer.test/api/products/search?name=laptop%20pro%2015"))
                .andRespond(withSuccess(risposta("[" + PRODOTTO_JSON + "]"), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca("laptop pro 15", null));

        assertThat(prodotti).extracting(ProdottoDTO::nome).containsExactly("Laptop Pro 15");
        server.verify();
    }

    @Test
    void filtroPerCategoriaUsaLEndpointCategory() {
        server.expect(requestTo("http://producer.test/api/products/category/Informatica"))
                .andRespond(withSuccess(risposta("[" + PRODOTTO_JSON + "]"), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca(null, "Informatica"));

        assertThat(prodotti).hasSize(1);
        server.verify();
    }

    @Test
    void nameAndCategoryKeepCategoryProductsWhoseNameContainsTheText() {
        server.expect(requestTo("http://producer.test/api/products/category/Informatica"))
                .andRespond(withSuccess(risposta("""
                        [
                          {"id": 1, "name": "Laptop Pro 15", "price": 1299.90, "category": "Informatica", "quantity": 15},
                          {"id": 3, "name": "Monitor 27 4K", "price": 449.90, "category": "Informatica", "quantity": 12}
                        ]
                        """), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca("monitor", "Informatica"));

        assertThat(prodotti).extracting(ProdottoDTO::id).containsExactly(3L);
        server.verify();
    }

    @Test
    void priceSortWithoutFiltersUsesTheSortEndpoint() {
        server.expect(requestTo("http://producer.test/api/products/sort?direction=DESC"))
                .andRespond(withSuccess(risposta("[" + PRODOTTO_JSON + "]"), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca(null, null, PriceSort.DESC));

        assertThat(prodotti).hasSize(1);
        server.verify();
    }

    @Test
    void priceSortWithFiltersSortsTheFilteredProductsByPrice() {
        server.expect(requestTo("http://producer.test/api/products/category/Informatica"))
                .andRespond(withSuccess(risposta("""
                        [
                          {"id": 1, "name": "Laptop Pro 15", "price": 1299.90, "category": "Informatica", "quantity": 15},
                          {"id": 3, "name": "Monitor 27 4K", "price": 449.90, "category": "Informatica", "quantity": 12},
                          {"id": 2, "name": "Laptop Air 13", "price": 899.00, "category": "Informatica", "quantity": 22}
                        ]
                        """), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca(null, "Informatica", PriceSort.ASC));

        assertThat(prodotti).extracting(ProdottoDTO::id).containsExactly(3L, 2L, 1L);
        server.verify();
    }

    @Test
    void trovaPerIdConverteIlJsonNelDto() {
        server.expect(requestTo("http://producer.test/api/products/1"))
                .andRespond(withSuccess(risposta(PRODOTTO_JSON), MediaType.APPLICATION_JSON));

        ProdottoDTO prodotto = client.trovaPerId(1L);

        assertThat(prodotto).isEqualTo(new ProdottoDTO(
                1L,
                "Laptop Pro 15",
                "Notebook professionale",
                new BigDecimal("1299.90"),
                "Informatica",
                15,
                LocalDateTime.of(2026, 9, 1, 9, 0)));
    }

    @Test
    void prodottoInesistenteProduceProdottoNonTrovatoConIlMessaggioDellApi() {
        server.expect(requestTo("http://producer.test/api/products/9999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                    "timestamp": "2026-09-28T18:30:00",
                                    "httpStatus": "404 NOT_FOUND",
                                    "error": "Not Found",
                                    "message": "Product not found with Id: 9999"
                                }
                                """));

        assertThatThrownBy(() -> client.trovaPerId(9999L))
                .isInstanceOf(ProdottoNonTrovatoException.class)
                .hasMessage("Product not found with Id: 9999");
    }

    @Test
    void erroreDelServerProduceApiErroreConLoStatus() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.trovaTutti(CriteriRicerca.nessuno()))
                .isInstanceOfSatisfying(ApiErroreException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(500));
    }

    @Test
    void producerNonRaggiungibileProduceApiNonDisponibile() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> client.trovaTutti(CriteriRicerca.nessuno()))
                .isInstanceOf(ApiNonDisponibileException.class);
    }

    static final String BASIC_ADMIN_SECRET = "Basic YWRtaW46c2VjcmV0";

    static ProductForm notebookGaming() {
        return new ProductForm("Notebook Gaming", "Notebook ad alte prestazioni",
                new BigDecimal("1599.90"), "Informatica", 8);
    }

    @Test
    void createProductSendsAnAuthenticatedPostWithTheProductAsJson() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", BASIC_ADMIN_SECRET))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Notebook Gaming"))
                .andExpect(jsonPath("$.price").value(1599.90))
                .andExpect(jsonPath("$.category").value("Informatica"))
                .andExpect(jsonPath("$.quantity").value(8))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body(risposta(PRODOTTO_JSON)));

        ProdottoDTO created = client.createProduct(notebookGaming());

        assertThat(created.id()).isEqualTo(1L);
        server.verify();
    }

    @Test
    void readRequestsAreSentWithoutCredentials() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andExpect(request -> assertThat(request.getHeaders().containsHeader("Authorization")).isFalse())
                .andRespond(withSuccess(risposta("[]"), MediaType.APPLICATION_JSON));

        client.trovaTutti(CriteriRicerca.nessuno());

        server.verify();
    }

    @Test
    void validationErrorsFromTheProducerBecomeFieldErrors() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {
                            "data": null,
                            "error": "Bad Request",
                            "errors": {"name": "Name is required", "price": "Price cannot be negative"},
                            "httpStatus": "400 BAD_REQUEST",
                            "message": "Some fields are invalid.",
                            "timestamp": "2026-10-07T10:00:00"
                        }
                        """));

        assertThatThrownBy(() -> client.createProduct(notebookGaming()))
                .isInstanceOfSatisfying(InvalidProductException.class, e -> {
                    assertThat(e.getMessage()).isEqualTo("Some fields are invalid.");
                    assertThat(e.getFieldErrors()).containsEntry("name", "Name is required")
                            .containsEntry("price", "Price cannot be negative");
                });
    }

    @Test
    void rejectedCredentialsBecomeApiAuthenticationException() {
        server.expect(requestTo("http://producer.test/api/products"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.createProduct(notebookGaming()))
                .isInstanceOf(ApiAuthenticationException.class);
    }
}
