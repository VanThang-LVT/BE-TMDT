package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.ProductStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String brand;
    private String keywords;
    private String specifications;
    private Integer mainImageId;
    private java.util.List<Integer> imageIds;
    private java.util.Map<String, String> attributes;
    private ProductStatus status;
    private java.util.List<ApprovalHistoryResponse> approvalHistories;
    private LocalDateTime createdAt;
}
