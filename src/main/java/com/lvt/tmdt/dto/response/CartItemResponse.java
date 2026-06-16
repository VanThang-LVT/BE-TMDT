package com.lvt.tmdt.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Integer productId;
    private String productName;
    private Long variantId;
    private String variantAttributes;
    private Integer shopId;
    private String shopName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
    private Integer stockQuantity;
}
