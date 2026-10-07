package com.savoia.productapi.service;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.exception.ResourceNotFoundException;
import com.savoia.productapi.payload.request.ProductRequest;
import com.savoia.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductRequest productRequest) {
        log.info("Creating product with name '{}' and category {}", productRequest.getName(), productRequest.getCategory());

        Product product = productRepository.save(ProductRequest.mapToEntity(productRequest));
        log.info("Product created successfully with id {}", product.getId());
        return product;
    }

    @Override
    public List<Product> findAll() {
        log.debug("Retrieving all products");

        List<Product> products = productRepository.findAll();
        log.debug("Retrieved {} products", products.size());
        return products;
    }

    @Override
    public Product findById(Long id) {
        log.debug("Retrieving product with id {}", id);
        return findProductById(id);
    }

    @Override
    public Product updateProduct(Long id, ProductRequest productRequest) {
        log.info("Updating product with id {}", id);

        Product product = findProductById(id);
        Product updatedProduct = ProductRequest.mapToEntity(productRequest);

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setCategory(updatedProduct.getCategory());
        product.setQuantity(updatedProduct.getQuantity());

        Product savedProduct = productRepository.save(product);
        log.info("Product with id {} updated successfully", savedProduct.getId());
        return savedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id {}", id);

        Product product = findProductById(id);
        productRepository.delete(product);
        log.info("Product with id {} deleted successfully", id);
    }

    @Override
    public List<Product> findByCategory(Category category) {
        log.debug("Searching products by category {}", category);

        List<Product> products = productRepository
                .findAll()
                .stream()
                .filter(p -> p.getCategory().equals(category))
                .toList();
        log.debug("Found {} products for category {}", products.size(), category);
        return products;
    }

    @Override
    public List<Product> searchByName(String name) {
        log.debug("Searching products by name '{}'", name);

        List<Product> products = productRepository.findAllByNameLike(name);
        log.debug("Found {} products matching name '{}'", products.size(), name);
        return products;
    }

    @Override
    public List<Product> sortByPrice(Sort.Direction direction) {
        log.debug("Retrieving products sorted by price in {} order", direction);

        List<Product> products = productRepository.findAll(Sort.by(direction, "price"));
        log.debug("Retrieved {} products sorted by price", products.size());
        return products;
    }

    protected Product findProductById(Long id){
        return productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product with id {} not found", id);
            return new ResourceNotFoundException("Product", "Id", id);
        });
    }
}
