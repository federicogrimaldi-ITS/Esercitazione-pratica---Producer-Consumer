package com.savoia.productclient.controller;

import com.savoia.productclient.exception.InvalidProductException;
import com.savoia.productclient.exception.ProdottoNonTrovatoException;
import com.savoia.productclient.model.Categoria;
import com.savoia.productclient.model.ProdottoDTO;
import com.savoia.productclient.model.ProductForm;
import com.savoia.productclient.service.ProdottoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProductFormController.class)
class ProductFormControllerTest {

    static final ProductForm NOTEBOOK = new ProductForm(
            "Notebook Gaming", "Notebook ad alte prestazioni", new BigDecimal("1599.90"), "Informatica", 8);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProdottoService prodottoService;

    @BeforeEach
    void categories() {
        when(prodottoService.categorie()).thenReturn(List.of(new Categoria("Accessori"), new Categoria("Informatica")));
    }

    @Test
    void newProductPageShowsAnEmptyFormWithTheCategories() throws Exception {
        mvc.perform(get("/prodotti/nuovo"))
                .andExpect(status().isOk())
                .andExpect(view().name("prodotti/form"))
                .andExpect(content().string(containsString("Nuovo prodotto")))
                .andExpect(content().string(containsString("action=\"/prodotti\"")))
                .andExpect(content().string(containsString("<option value=\"Informatica\">Informatica</option>")));
    }

    @Test
    void validProductIsCreatedAndTheUserIsRedirectedToItsDetail() throws Exception {
        when(prodottoService.createProduct(NOTEBOOK)).thenReturn(
                new ProdottoDTO(21L, "Notebook gaming", null, new BigDecimal("1599.90"), "Informatica", 8, null));

        mvc.perform(post("/prodotti")
                        .param("name", "Notebook Gaming")
                        .param("description", "Notebook ad alte prestazioni")
                        .param("price", "1599.90")
                        .param("category", "Informatica")
                        .param("quantity", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prodotti/21"))
                .andExpect(flash().attribute("successMessage", "Prodotto creato."));
    }

    @Test
    void producerValidationErrorsAreShownUnderTheFields() throws Exception {
        when(prodottoService.createProduct(any())).thenThrow(new InvalidProductException(
                "Some fields are invalid.", Map.of("name", "Name is required")));

        mvc.perform(post("/prodotti").param("name", "").param("price", "10").param("category", "Informatica"))
                .andExpect(status().isOk())
                .andExpect(view().name("prodotti/form"))
                .andExpect(content().string(containsString("Some fields are invalid.")))
                .andExpect(content().string(containsString("Name is required")))
                .andExpect(content().string(containsString("<option value=\"Informatica\" selected=\"selected\">")));
    }

    @Test
    void nonNumericPriceIsRejectedWithoutCallingTheProducer() throws Exception {
        mvc.perform(post("/prodotti").param("name", "Mouse").param("price", "abc").param("category", "Accessori"))
                .andExpect(status().isOk())
                .andExpect(view().name("prodotti/form"))
                .andExpect(content().string(containsString("Valore non valido")))
                .andExpect(content().string(containsString("value=\"abc\"")));

        verify(prodottoService, atLeastOnce()).categorie();
        verifyNoMoreInteractions(prodottoService);
    }

    static final ProdottoDTO LAPTOP = new ProdottoDTO(1L, "Laptop Pro 15", "Notebook professionale",
            new BigDecimal("1299.90"), "Informatica", 15, null);

    @Test
    void editPageShowsTheFormFilledWithTheCurrentProduct() throws Exception {
        when(prodottoService.trovaProdotto(1L)).thenReturn(LAPTOP);

        mvc.perform(get("/prodotti/1/modifica"))
                .andExpect(status().isOk())
                .andExpect(view().name("prodotti/form"))
                .andExpect(content().string(containsString("Modifica prodotto")))
                .andExpect(content().string(containsString("action=\"/prodotti/1\"")))
                .andExpect(content().string(containsString("value=\"Laptop Pro 15\"")))
                .andExpect(content().string(containsString("value=\"1299.90\"")))
                .andExpect(content().string(containsString("<option value=\"Informatica\" selected=\"selected\">")));
    }

    @Test
    void editPageOfAMissingProductShowsNotFound() throws Exception {
        when(prodottoService.trovaProdotto(9999L)).thenThrow(new ProdottoNonTrovatoException("Product not found with Id: 9999"));

        mvc.perform(get("/prodotti/9999/modifica"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("errore"));
    }

    @Test
    void validChangesAreSavedAndTheUserIsRedirectedToTheDetail() throws Exception {
        when(prodottoService.updateProduct(1L, NOTEBOOK)).thenReturn(LAPTOP);

        mvc.perform(post("/prodotti/1")
                        .param("name", "Notebook Gaming")
                        .param("description", "Notebook ad alte prestazioni")
                        .param("price", "1599.90")
                        .param("category", "Informatica")
                        .param("quantity", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prodotti/1"))
                .andExpect(flash().attribute("successMessage", "Prodotto aggiornato."));
    }

    @Test
    void invalidChangesShowTheEditFormAgain() throws Exception {
        when(prodottoService.updateProduct(any(), any())).thenThrow(new InvalidProductException(
                "Some fields are invalid.", Map.of("price", "Price cannot be negative")));

        mvc.perform(post("/prodotti/1").param("name", "Laptop").param("price", "-1").param("category", "Informatica"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Modifica prodotto")))
                .andExpect(content().string(containsString("action=\"/prodotti/1\"")))
                .andExpect(content().string(containsString("Price cannot be negative")));
    }

    @Test
    void deletingAProductRedirectsToTheListWithAMessage() throws Exception {
        mvc.perform(post("/prodotti/20/elimina"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prodotti"))
                .andExpect(flash().attribute("successMessage", "Prodotto eliminato."));

        verify(prodottoService).deleteProduct(20L);
    }

    @Test
    void deletingAMissingProductShowsNotFound() throws Exception {
        doThrow(new ProdottoNonTrovatoException("Product not found with Id: 9999"))
                .when(prodottoService).deleteProduct(9999L);

        mvc.perform(post("/prodotti/9999/elimina"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("errore"));
    }
}
