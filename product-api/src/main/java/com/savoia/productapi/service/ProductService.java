package com.savoia.productapi.service;

import com.savoia.productapi.enums.Category;
import com.savoia.productapi.payload.request.ProductRequest;
import com.savoia.productapi.payload.response.ProductResponse;
import com.savoia.productapi.payload.response.ProductResponseDetails;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ProductService {

    ProductResponseDetails createProduct(ProductRequest productRequest);

    List<ProductResponse> findAll();

    ProductResponseDetails findById(Long id);

    ProductResponseDetails updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);

    List<ProductResponse> findByCategory(Category category);

    List<ProductResponse> searchByName(String name);

    List<ProductResponse> sortByPrice(Sort.Direction direction);
}
