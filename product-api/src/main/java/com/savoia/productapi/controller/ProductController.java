package com.savoia.productapi.controller;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.payload.request.ProductRequest;
import com.savoia.productapi.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody ProductRequest productRequest){
        return productService.createProduct(productRequest);
    }

    @GetMapping
    public List<Product> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Product findById(
            @PathVariable("id") @Positive(message = "L'id deve essere positivo") Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable("id") @Positive(message = "L'id deve essere positivo") Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        return productService.updateProduct(id, productRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable("id") @Positive(message = "L'id deve essere positivo") Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/category/{category}")
    public List<Product> findByCategory(
            @PathVariable("category") @NotNull(message = "La categoria è obbligatoria") Category category) {
        return productService.findByCategory(category);
    }

    @GetMapping("/search")
    public List<Product> searchByName(
            @RequestParam("name")
            @NotBlank(message = "Il nome è obbligatorio")
            @Size(max = 200, message = "Il nome non può superare i 200 caratteri") String name) {
        return productService.searchByName(name);
    }

    @GetMapping("/sort")
    public List<Product> sortByPrice(
            @RequestParam(name = "direction", defaultValue = "ASC")
            @NotNull(message = "La direzione è obbligatoria") Sort.Direction direction) {
        return productService.sortByPrice(direction);
    }
}
