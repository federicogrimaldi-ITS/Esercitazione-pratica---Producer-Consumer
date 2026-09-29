package com.savoia.productapi.service;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.payload.request.ProductRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ProductService {

    Product createProduct(ProductRequest productRequest);

    List<Product> findAll();

    Product findById(Long id);

    Product updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);

    List<Product> findByCategory(Category category);

    List<Product> searchByName(String name);

    List<Product> sortByPrice(Sort.Direction direction);
}
