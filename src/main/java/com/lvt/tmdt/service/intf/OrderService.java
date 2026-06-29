package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.OrderRequest;
import com.lvt.tmdt.dto.response.OrderResponse;
import com.lvt.tmdt.dto.response.ShopOrderResponse;
import com.lvt.tmdt.enums.ShopOrderStatus;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Map;

public interface OrderService {
    OrderResponse placeOrder(Integer userId, OrderRequest request);
    List<OrderResponse> getMyOrders(Integer userId);
    Page<ShopOrderResponse> getSellerOrders(Integer userId, String keyword, List<ShopOrderStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    ShopOrderResponse updateShopOrderStatus(Integer userId, Long shopOrderId, ShopOrderStatus status, String cancelReason);
    Map<String, Long> getOrderCountsByStatus(Integer userId);
}
