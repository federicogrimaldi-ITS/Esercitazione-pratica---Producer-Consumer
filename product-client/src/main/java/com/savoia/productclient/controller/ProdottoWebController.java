package com.savoia.productclient.controller;

import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.service.ProdottoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProdottoWebController {

    private final ProdottoService prodottoService;

    public ProdottoWebController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/prodotti";
    }

    @GetMapping("/prodotti")
    public String lista(Model model) {
        model.addAttribute("prodotti", prodottoService.cercaProdotti(CriteriRicerca.nessuno()));
        return "prodotti/lista";
    }

    @GetMapping("/prodotti/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("prodotto", prodottoService.trovaProdotto(id));
        return "prodotti/dettaglio";
    }
}
