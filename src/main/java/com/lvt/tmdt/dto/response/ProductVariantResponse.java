package com.lvt.tmdt.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductVariantResponse {
    private Long variantId;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Map<String, String> attributes;
}
