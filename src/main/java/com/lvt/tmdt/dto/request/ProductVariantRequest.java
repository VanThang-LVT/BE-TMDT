package com.lvt.tmdt.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductVariantRequest {
    private Long variantId;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Map<Integer, String> attributes;
}
