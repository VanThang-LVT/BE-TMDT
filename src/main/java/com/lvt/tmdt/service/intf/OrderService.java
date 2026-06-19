package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.OrderRequest;
import com.lvt.tmdt.dto.response.OrderResponse;

public interface OrderService {
    OrderResponse placeOrder(Integer userId, OrderRequest request);
    java.util.List<OrderResponse> getMyOrders(Integer userId);
}
