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

    static final String PRODOTTO_JSON = """
            {
                "id": 1,
                "nome": "Laptop Pro 15",
                "descrizione": "Notebook professionale",
                "prezzo": 1299.90,
                "categoria": "Informatica",
                "quantita": 15,
                "dataCreazione": "2026-09-01T09:00:00"
            }
            """;

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
        server.expect(requestTo("http://producer.test/api/prodotti"))
                .andExpect(method(GET))
                .andRespond(withSuccess("[" + PRODOTTO_JSON + "]", MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(CriteriRicerca.nessuno());

        assertThat(prodotti).extracting(ProdottoDTO::nome).containsExactly("Laptop Pro 15");
        server.verify();
    }

    @Test
    void trovaTuttiInoltraNomeECategoriaComeQueryParam() {
        server.expect(requestTo("http://producer.test/api/prodotti?nome=laptop&categoria=Informatica"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<ProdottoDTO> prodotti = client.trovaTutti(new CriteriRicerca("laptop", "Informatica"));

        assertThat(prodotti).isEmpty();
        server.verify();
    }

    @Test
    void trovaPerIdConverteIlJsonNelDto() {
        server.expect(requestTo("http://producer.test/api/prodotti/1"))
                .andRespond(withSuccess(PRODOTTO_JSON, MediaType.APPLICATION_JSON));

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
        server.expect(requestTo("http://producer.test/api/prodotti/9999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"status": 404, "message": "Prodotto non trovato", "timestamp": "2026-09-28T18:30:00"}
                                """));

        assertThatThrownBy(() -> client.trovaPerId(9999L))
                .isInstanceOf(ProdottoNonTrovatoException.class)
                .hasMessage("Prodotto non trovato");
    }

    @Test
    void erroreDelServerProduceApiErroreConLoStatus() {
        server.expect(requestTo("http://producer.test/api/prodotti"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.trovaTutti(CriteriRicerca.nessuno()))
                .isInstanceOfSatisfying(ApiErroreException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(500));
    }

    @Test
    void producerNonRaggiungibileProduceApiNonDisponibile() {
        server.expect(requestTo("http://producer.test/api/prodotti"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> client.trovaTutti(CriteriRicerca.nessuno()))
                .isInstanceOf(ApiNonDisponibileException.class);
    }
}
