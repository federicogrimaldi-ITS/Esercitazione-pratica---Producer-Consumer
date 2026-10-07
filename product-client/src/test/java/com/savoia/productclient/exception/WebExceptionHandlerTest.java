package com.savoia.productclient.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WebExceptionHandlerTest.ControllerCheFallisce.class)
@Import(WebExceptionHandlerTest.ControllerCheFallisce.class)
class WebExceptionHandlerTest {

    @Controller
    static class ControllerCheFallisce {

        @GetMapping("/api-giu")
        String apiGiu() {
            throw new ApiNonDisponibileException(new RuntimeException("Connection refused"));
        }

        @GetMapping("/non-trovato")
        String nonTrovato() {
            throw new ProdottoNonTrovatoException("Prodotto non trovato");
        }

        @GetMapping("/errore-api")
        String erroreApi() {
            throw new ApiErroreException(500, "Errore interno");
        }
    }

    @Autowired
    MockMvc mvc;

    @Test
    void apiNonDisponibileMostraIlMessaggioDiServizioNonDisponibile() throws Exception {
        mvc.perform(get("/api-giu"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("errore"))
                .andExpect(content().string(containsString("Impossibile recuperare i prodotti.")))
                .andExpect(content().string(containsString("Il servizio API non è attualmente disponibile.")));
    }

    @Test
    void prodottoNonTrovatoRestituisce404() throws Exception {
        mvc.perform(get("/non-trovato"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("errore"))
                .andExpect(content().string(containsString("Prodotto non trovato")));
    }

    @Test
    void erroreDellApiRestituisce502ConIlMessaggio() throws Exception {
        mvc.perform(get("/errore-api"))
                .andExpect(status().isBadGateway())
                .andExpect(view().name("errore"))
                .andExpect(content().string(containsString("Errore interno")));
    }
}
