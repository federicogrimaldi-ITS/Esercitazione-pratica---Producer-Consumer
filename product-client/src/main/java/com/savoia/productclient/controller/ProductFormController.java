package com.savoia.productclient.controller;

import com.savoia.productclient.exception.InvalidProductException;
import com.savoia.productclient.model.ProdottoDTO;
import com.savoia.productclient.model.ProductForm;
import com.savoia.productclient.service.ProdottoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
public class ProductFormController {

    private static final String FORM_VIEW = "prodotti/form";
    private static final Set<String> FORM_FIELDS = Set.of("name", "description", "price", "category", "quantity");

    private final ProdottoService prodottoService;

    public ProductFormController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @GetMapping("/prodotti/nuovo")
    public String newProduct(Model model) {
        return showForm(model, new ProductForm(), "Nuovo prodotto", "/prodotti");
    }

    @PostMapping("/prodotti")
    public String createProduct(@ModelAttribute ProductForm productForm, BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showForm(model, productForm, "Nuovo prodotto", "/prodotti");
        }
        try {
            ProdottoDTO created = prodottoService.createProduct(productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Prodotto creato.");
            return "redirect:/prodotti/" + created.id();
        } catch (InvalidProductException ex) {
            addProducerErrors(ex, bindingResult);
            return showForm(model, productForm, "Nuovo prodotto", "/prodotti");
        }
    }

    private String showForm(Model model, ProductForm productForm, String title, String action) {
        model.addAttribute("productForm", productForm);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("categories", prodottoService.categorie());
        return FORM_VIEW;
    }

    private static void addProducerErrors(InvalidProductException ex, BindingResult bindingResult) {
        bindingResult.reject("producer", ex.getMessage());
        ex.getFieldErrors().forEach((field, message) -> {
            if (FORM_FIELDS.contains(field)) {
                bindingResult.rejectValue(field, "producer", message);
            }
        });
    }
}
