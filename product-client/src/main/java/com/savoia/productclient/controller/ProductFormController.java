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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;
import java.util.function.Supplier;

@Controller
public class ProductFormController {

    private static final String FORM_VIEW = "prodotti/form";
    private static final String CREATE_TITLE = "Nuovo prodotto";
    private static final String EDIT_TITLE = "Modifica prodotto";
    private static final Set<String> FORM_FIELDS = Set.of("name", "description", "price", "category", "quantity");

    private final ProdottoService prodottoService;

    public ProductFormController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @GetMapping("/prodotti/nuovo")
    public String newProduct(Model model) {
        return showForm(model, new ProductForm(), CREATE_TITLE, "/prodotti");
    }

    @PostMapping("/prodotti")
    public String createProduct(@ModelAttribute ProductForm productForm, BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        return save(productForm, bindingResult, model, redirectAttributes, CREATE_TITLE, "/prodotti",
                () -> prodottoService.createProduct(productForm), "Prodotto creato.");
    }

    @GetMapping("/prodotti/{id}/modifica")
    public String editProduct(@PathVariable Long id, Model model) {
        ProductForm productForm = ProductForm.from(prodottoService.trovaProdotto(id));
        return showForm(model, productForm, EDIT_TITLE, "/prodotti/" + id);
    }

    @PostMapping("/prodotti/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductForm productForm,
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        return save(productForm, bindingResult, model, redirectAttributes, EDIT_TITLE, "/prodotti/" + id,
                () -> prodottoService.updateProduct(id, productForm), "Prodotto aggiornato.");
    }

    private String save(ProductForm productForm, BindingResult bindingResult, Model model,
                        RedirectAttributes redirectAttributes, String title, String action,
                        Supplier<ProdottoDTO> producerCall, String successMessage) {
        if (bindingResult.hasErrors()) {
            return showForm(model, productForm, title, action);
        }
        try {
            ProdottoDTO saved = producerCall.get();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/prodotti/" + saved.id();
        } catch (InvalidProductException ex) {
            addProducerErrors(ex, bindingResult);
            return showForm(model, productForm, title, action);
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
