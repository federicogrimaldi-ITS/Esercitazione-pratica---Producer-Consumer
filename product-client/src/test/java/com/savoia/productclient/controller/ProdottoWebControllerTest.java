package com.savoia.productclient.controller;

import com.savoia.productclient.exception.ApiNonDisponibileException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.Categoria;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import com.savoia.productclient.service.ProdottoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProdottoWebController.class)
class ProdottoWebControllerTest {

    static final ProdottoDTO LAPTOP = new ProdottoDTO(1L, "Laptop Pro 15",
            "Notebook professionale con processore Intel Core i7, 16 GB di RAM e SSD da 512 GB.",
            new BigDecimal("1299.90"), "INFORMATICA", 15, LocalDateTime.of(2026, 9, 1, 9, 0));

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProdottoService prodottoService;

    @Test
    void laRootReindirizzaAllaListaProdotti() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prodotti"));
    }

    @Test
    void listaMostraIProdottiInTabellaConPrezzoFormattato() throws Exception {
        when(prodottoService.cercaProdotti(CriteriRicerca.nessuno())).thenReturn(List.of(LAPTOP));

        mvc.perform(get("/prodotti"))
                .andExpect(status().isOk())
                .andExpect(view().name("prodotti/lista"))
                .andExpect(content().string(containsString("<table")))
                .andExpect(content().string(containsString("Laptop Pro 15")))
                .andExpect(content().string(containsString("<td>Informatica</td>")))
                .andExpect(content().string(not(containsString("INFORMATICA"))))
                .andExpect(content().string(containsString("€ 1.299,90")))
                .andExpect(content().string(containsString("href=\"/prodotti/1\"")));
    }

    @Test
    void listaVuotaMostraUnMessaggio() throws Exception {
        when(prodottoService.cercaProdotti(CriteriRicerca.nessuno())).thenReturn(List.of());

        mvc.perform(get("/prodotti"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nessun prodotto trovato")));
    }

    @Test
    void producerNonRaggiungibileMostraLaPaginaDiErrore() throws Exception {
        when(prodottoService.cercaProdotti(CriteriRicerca.nessuno()))
                .thenThrow(new ApiNonDisponibileException(new RuntimeException("Connection refused")));

        mvc.perform(get("/prodotti"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("Il servizio API non è attualmente disponibile.")));
    }

    @Test
    void dettaglioMostraTuttiIDatiDelProdotto() throws Exception {
        when(prodottoService.trovaProdotto(1L)).thenReturn(LAPTOP);

        mvc.perform(get("/prodotti/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("prodotti/dettaglio"))
                .andExpect(content().string(containsString("Laptop Pro 15")))
                .andExpect(content().string(containsString("<dd>Informatica</dd>")))
                .andExpect(content().string(containsString("€ 1.299,90")))
                .andExpect(content().string(containsString("15")))
                .andExpect(content().string(containsString("Notebook professionale con processore Intel Core i7")))
                .andExpect(content().string(containsString("01/09/2026 09:00")));
    }

    @Test
    void dettaglioDiUnProdottoInesistenteRestituisce404() throws Exception {
        when(prodottoService.trovaProdotto(9999L)).thenThrow(new ProdottoNonTrovatoException("Prodotto non trovato"));

        mvc.perform(get("/prodotti/9999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("errore"))
                .andExpect(content().string(containsString("Prodotto non trovato")));
    }

    @Test
    void idNonNumericoRestituisce400() throws Exception {
        mvc.perform(get("/prodotti/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ricercaInoltraNomeECategoriaEMantieneIValoriNelForm() throws Exception {
        when(prodottoService.cercaProdotti(new CriteriRicerca("laptop", "INFORMATICA"))).thenReturn(List.of(LAPTOP));
        when(prodottoService.categorie()).thenReturn(List.of(new Categoria("ACCESSORI"), new Categoria("INFORMATICA")));

        mvc.perform(get("/prodotti").param("nome", "laptop").param("categoria", "INFORMATICA"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laptop Pro 15")))
                .andExpect(content().string(containsString("name=\"nome\" value=\"laptop\"")))
                .andExpect(content().string(containsString("<option value=\"INFORMATICA\" selected=\"selected\">Informatica</option>")))
                .andExpect(content().string(containsString("<option value=\"ACCESSORI\">Accessori</option>")));
    }

    @Test
    void ricercaConCampiVuotiEquivaleAllaListaCompleta() throws Exception {
        when(prodottoService.cercaProdotti(CriteriRicerca.nessuno())).thenReturn(List.of(LAPTOP));

        mvc.perform(get("/prodotti").param("nome", "").param("categoria", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laptop Pro 15")))
                .andExpect(content().string(containsString("<option value=\"\" selected=\"selected\">Tutte</option>")));
    }
}
