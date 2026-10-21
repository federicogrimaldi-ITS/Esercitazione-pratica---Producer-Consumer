package com.savoia.productapi.payload.response;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDetails {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private Integer quantity;

    public static ProductResponseDetails mapToResponse(Product product) {
        return ProductResponseDetails.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .quantity(product.getQuantity())
                .build();
    }
}
