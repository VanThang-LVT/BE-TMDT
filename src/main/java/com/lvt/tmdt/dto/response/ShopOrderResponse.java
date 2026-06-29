package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.ShopOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOrderResponse {
    private Long shopOrderId;
    private Integer shopId;
    private String shopName;
    private BigDecimal subtotalAmount;
    private BigDecimal commissionAmount;
    private BigDecimal sellerAmount;
    private ShopOrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> orderItems;
    private List<OrderStatusHistoryResponse> statusHistories;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String cancelReason;
}
