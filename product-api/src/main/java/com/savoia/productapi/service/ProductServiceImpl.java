package com.savoia.productapi.service;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.exception.ResourceNotFoundException;
import com.savoia.productapi.payload.request.ProductRequest;
import com.savoia.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductRequest productRequest) {
        return productRepository.save(ProductRequest.mapToEntity(productRequest));
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return findProductById(id);
    }

    @Override
    public Product updateProduct(Long id, ProductRequest productRequest) {
        Product product = findProductById(id);
        Product updatedProduct = ProductRequest.mapToEntity(productRequest);

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setCategory(updatedProduct.getCategory());
        product.setQuantity(updatedProduct.getQuantity());

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return productRepository
                .findAll()
                .stream()
                .filter(p -> p.getCategory().equals(category))
                .toList();
    }

    @Override
    public List<Product> searchByName(String name) {
        return productRepository
                .findAll()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .toList();
    }

    @Override
    public List<Product> sortByPrice(Sort.Direction direction) {
        return productRepository.findAll(Sort.by(direction, "price"));
    }

    protected Product findProductById(Long id){
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product","Id", id));
    }
}
