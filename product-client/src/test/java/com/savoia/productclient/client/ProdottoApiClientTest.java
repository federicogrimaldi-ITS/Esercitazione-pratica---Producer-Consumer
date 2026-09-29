package com.savoia.productclient.client;

import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.exception.ApiErroreException;
import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.CriteriRicerca;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

@RestClientTest(ProdottoApiClient.class)
@TestPropertySource(properties = "api.base-url=http://producer.test/api")
class ProdottoApiClientTest {

    /** Prodotto come lo serializza la Producer (entity Prodotto, campi in inglese, categoria enum). */
    static final String PRODOTTO_JSON = """
            {
                "id": 1,
                "name": "Laptop Pro 15",
                "description": "Notebook professionale",
                "price": 1299.90,
                "category": "INFORMATICA",
                "quantity": 15,
                "dataCreazione": "2026-09-01T09:00:00"
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
        server.expect(requestTo("http://producer.test/api/products/category/INFORMATICA"))
                .andRespond(withSuccess(risposta("[" + PRODOTTO_JSON + "]"), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca(null, "informatica"));

        assertThat(prodotti).hasSize(1);
        server.verify();
    }

    @Test
    void nomeECategoriaInsiemeFiltranoPerNomeIProdottiDellaCategoria() {
        server.expect(requestTo("http://producer.test/api/products/category/INFORMATICA"))
                .andRespond(withSuccess(risposta("""
                        [
                          {"id": 1, "name": "Laptop Pro 15", "price": 1299.90, "category": "INFORMATICA", "quantity": 15},
                          {"id": 3, "name": "Monitor 27 4K", "price": 449.90, "category": "INFORMATICA", "quantity": 12}
                        ]
                        """), MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca("monitor 27 4k", "INFORMATICA"));

        assertThat(prodotti).extracting(ProdottoDTO::id).containsExactly(3L);
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
                "INFORMATICA",
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
}
