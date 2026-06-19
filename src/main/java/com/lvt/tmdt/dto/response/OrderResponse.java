package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.OrderStatus;
import com.lvt.tmdt.enums.PaymentMethod;
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
public class OrderResponse {
    private Long orderId;
    private Integer userId;
    private Integer voucherId;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<ShopOrderResponse> shopOrders;
}
