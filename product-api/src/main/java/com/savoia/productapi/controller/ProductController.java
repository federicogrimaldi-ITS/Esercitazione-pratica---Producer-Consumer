package com.savoia.productapi.controller;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.payload.request.ProductRequest;
import com.savoia.productapi.payload.response.ResponseApi;
import com.savoia.productapi.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.createProduct(productRequest);
        return ResponseApi.buildResponse(
                HttpStatus.CREATED,
                product,
                "Product created successfully");
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable("id") @Positive(message = "The ID must be positive") Long id) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.findById(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<?> updateProduct(
            @PathVariable("id") @Positive(message = "The ID must be positive") Long id,
            @Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.updateProduct(id, productRequest);
        return ResponseApi.buildResponse(
                HttpStatus.OK,
                product,
                "Product updated successfully");
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<?> deleteProduct(
            @PathVariable("id") @Positive(message = "The ID must be positive") Long id) {
        productService.deleteProduct(id);
        return ResponseApi.buildResponse(
                HttpStatus.NO_CONTENT,
                "Product deleted successfully");
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> findByCategory(
            @PathVariable("category") @NotNull(message = "Category is required") Category category) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.findByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByName(
            @RequestParam("name")
            @NotBlank(message = "Name is required")
            @Size(max = 200, message = "Name cannot exceed 200 characters") String name) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.searchByName(name));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> sortByPrice(
            @RequestParam(name = "direction", defaultValue = "ASC")
            @NotNull(message = "Direction is required") Sort.Direction direction) {
        return ResponseApi.buildResponse(HttpStatus.OK, productService.sortByPrice(direction));
    }
}
