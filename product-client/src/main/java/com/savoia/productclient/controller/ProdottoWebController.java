package com.savoia.productclient.controller;

import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.service.ProdottoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String lista(@RequestParam(required = false) String nome,
                        @RequestParam(required = false) String categoria,
                        Model model) {
        CriteriRicerca criteri = new CriteriRicerca(nome, categoria);
        model.addAttribute("prodotti", prodottoService.cercaProdotti(criteri));
        model.addAttribute("categorie", prodottoService.categorie());
        model.addAttribute("criteri", criteri);
        return "prodotti/lista";
    }

    @GetMapping("/prodotti/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("prodotto", prodottoService.trovaProdotto(id));
        return "prodotti/dettaglio";
    }
}
