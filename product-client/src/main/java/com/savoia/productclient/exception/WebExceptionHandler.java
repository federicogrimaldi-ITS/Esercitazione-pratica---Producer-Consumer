package com.savoia.productclient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Trasforma gli errori della comunicazione con la Producer API in una pagina HTML leggibile.
 */
@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(ApiNonDisponibileException.class)
    public ModelAndView apiNonDisponibile(ApiNonDisponibileException ex) {
        return paginaErrore(HttpStatus.SERVICE_UNAVAILABLE,
                "Impossibile recuperare i prodotti.",
                "Il servizio API non è attualmente disponibile.");
    }

    @ExceptionHandler(ProdottoNonTrovatoException.class)
    public ModelAndView prodottoNonTrovato(ProdottoNonTrovatoException ex) {
        return paginaErrore(HttpStatus.NOT_FOUND, "Prodotto non trovato", ex.getMessage());
    }

    @ExceptionHandler(ApiErroreException.class)
    public ModelAndView erroreApi(ApiErroreException ex) {
        return paginaErrore(HttpStatus.BAD_GATEWAY,
                "Errore dal servizio prodotti (HTTP " + ex.getStatus() + ")", ex.getMessage());
    }

    private ModelAndView paginaErrore(HttpStatus status, String titolo, String messaggio) {
        ModelAndView mav = new ModelAndView("errore", status);
        mav.addObject("titolo", titolo);
        mav.addObject("messaggio", messaggio);
        return mav;
    }
}
