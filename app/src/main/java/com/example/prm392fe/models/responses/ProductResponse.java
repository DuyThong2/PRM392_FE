package com.example.prm392fe.models.responses;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private int id;
    private String name;
    private String briefDescription;
    private String fullDescription;
    private String technicalSpecification;
    private BigDecimal price;
    private int quantity;
    private int categoryId;
    private String categoryName;
    private List<String> imageUrls;
}
