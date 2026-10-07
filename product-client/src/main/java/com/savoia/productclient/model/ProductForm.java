package com.savoia.productclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductForm {

    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer quantity;
}
