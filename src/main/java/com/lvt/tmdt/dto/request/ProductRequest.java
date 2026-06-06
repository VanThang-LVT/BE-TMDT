package com.lvt.tmdt.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Short categoryId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String brand;
    private String keywords;
    private String specifications;
    private java.util.Map<Integer, String> attributes;
    private java.util.List<ProductVariantRequest> variants;
    private java.util.List<Integer> existingImageIdsToKeep;
    private Integer mainImageId;
}
