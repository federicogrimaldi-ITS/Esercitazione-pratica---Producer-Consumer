package com.savoia.productclient.client;

import com.savoia.productclient.model.CriteriRicerca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(properties = "api.base-url=http://producer.test/api")
@AutoConfigureMockRestServiceServer
class ProdottoApiClientCacheTest {

    @Autowired
    ProdottoApiClient client;

    @Autowired
    MockRestServiceServer server;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void svuotaCache() {
        cacheManager.getCacheNames().forEach(nome -> cacheManager.getCache(nome).clear());
    }

    @Test
    void laStessaRicercaRipetutaEffettuaUnaSolaChiamataHttp() {
        server.expect(once(), requestTo("http://producer.test/api/products/search?name=laptop"))
                .andRespond(withSuccess(ProdottoApiClientTest.risposta("[]"), MediaType.APPLICATION_JSON));

        client.trovaTutti(new CriteriRicerca("laptop", null));
        client.trovaTutti(new CriteriRicerca(" laptop ", ""));

        server.verify();
    }

    @Test
    void ilDettaglioRipetutoEffettuaUnaSolaChiamataHttp() {
        server.expect(once(), requestTo("http://producer.test/api/products/1"))
                .andRespond(withSuccess(ProdottoApiClientTest.risposta(ProdottoApiClientTest.PRODOTTO_JSON), MediaType.APPLICATION_JSON));

        client.trovaPerId(1L);
        client.trovaPerId(1L);

        server.verify();
    }

    @Test
    void gliErroriNonVengonoMessiInCache() {
        server.expect(once(), requestTo("http://producer.test/api/products"))
                .andRespond(withException(new IOException("Connection refused")));
        server.expect(once(), requestTo("http://producer.test/api/products"))
                .andRespond(withSuccess(ProdottoApiClientTest.risposta("[]"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.trovaTutti(CriteriRicerca.nessuno()));
        client.trovaTutti(CriteriRicerca.nessuno());

        server.verify();
    }

    @Test
    void laCacheEInMemoriaConCaffeine() {
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder("prodotti", "prodotto");
    }

    @Test
    void creatingAProductEmptiesTheCacheSoTheListIsReloaded() {
        server.expect(once(), requestTo("http://producer.test/api/products"))
                .andExpect(method(GET))
                .andRespond(withSuccess(ProdottoApiClientTest.risposta("[]"), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://producer.test/api/products"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body(ProdottoApiClientTest.risposta(ProdottoApiClientTest.PRODOTTO_JSON)));
        server.expect(once(), requestTo("http://producer.test/api/products"))
                .andExpect(method(GET))
                .andRespond(withSuccess(ProdottoApiClientTest.risposta("[]"), MediaType.APPLICATION_JSON));

        client.trovaTutti(CriteriRicerca.nessuno());
        client.createProduct(ProdottoApiClientTest.notebookGaming());
        client.trovaTutti(CriteriRicerca.nessuno());

        server.verify();
    }
}
