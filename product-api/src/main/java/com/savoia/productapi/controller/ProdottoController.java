package com.savoia.productapi.controller;

import com.savoia.productapi.payload.request.ProdottoRequest;
import com.savoia.productapi.payload.response.ResponseApi;
import com.savoia.productapi.service.ProdottoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProdottoController {

    private final ProdottoService prodottoService;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProdottoRequest prodottoRequest){
        return ResponseApi.buildResponse(HttpStatus.CREATED,prodottoService.createProduct(prodottoRequest));
    }
}
