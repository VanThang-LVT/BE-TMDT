package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.ProductStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ProductResponse {
    private Integer productId;
    private Integer shopId;
    private String shopName;
    private Short categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer salesCount;
    private String brand;
    private String keywords;
    private String specifications;
    private Integer mainImageId;
    private List<Integer> imageIds;
    private Map<String, String> attributes;
    private ProductStatus status;
    private List<ApprovalHistoryResponse> approvalHistories;
    private List<ProductVariantResponse> variants;
    private LocalDateTime createdAt;
}
