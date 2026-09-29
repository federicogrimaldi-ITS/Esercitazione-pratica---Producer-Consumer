package com.savoia.productapi.controller;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.payload.request.ProductRequest;
import com.savoia.productapi.payload.response.ResponseApi;
import com.savoia.productapi.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.createProduct(productRequest);
        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                product,
                "Prodotto creato con successo");
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable("id") @Positive(message = "L'id deve essere positivo") Long id) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("id") @Positive(message = "L'id deve essere positivo") Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.updateProduct(id, productRequest);
        return ResponseApi.buildResponse(
                HttpStatus.OK,
                product,
                "Prodotto aggiornato con successo");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable("id") @Positive(message = "L'id deve essere positivo") Long id) {
        productService.deleteProduct(id);
        return ResponseApi.buildResponse(
                HttpStatus.NO_CONTENT,
                "Prodotto eliminato con successo");
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> findByCategory(
            @PathVariable("category") @NotNull(message = "La categoria è obbligatoria") Category category) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.findByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByName(
            @RequestParam("name")
            @NotBlank(message = "Il nome è obbligatorio")
            @Size(max = 200, message = "Il nome non può superare i 200 caratteri") String name) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.searchByName(name));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> sortByPrice(
            @RequestParam(name = "direction", defaultValue = "ASC")
            @NotNull(message = "La direzione è obbligatoria") Sort.Direction direction) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.sortByPrice(direction));
    }
}
