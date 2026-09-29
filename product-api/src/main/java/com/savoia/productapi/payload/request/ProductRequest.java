package com.savoia.productapi.payload.request;

import com.savoia.productapi.entity.Product;
import com.savoia.productapi.enums.Category;
import com.savoia.productapi.utils.StringUtility;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private Category category;

    @Builder.Default
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;

    public static Product mapToEntity(ProductRequest productRequest){
        return Product.builder()
                .name(StringUtility.formatText(productRequest.getName()))
                .description(StringUtility.formatText(productRequest.getDescription()))
                .price(productRequest.getPrice())
                .category(productRequest.getCategory())
                .quantity(productRequest.getQuantity() == null ? 0 : productRequest.getQuantity())
                .build();
    }

}
