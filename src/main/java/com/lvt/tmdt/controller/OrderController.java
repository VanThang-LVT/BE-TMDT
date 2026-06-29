package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.ApiResponse;
import com.lvt.tmdt.dto.request.OrderRequest;
import com.lvt.tmdt.dto.response.OrderResponse;
import com.lvt.tmdt.sercurity.CustomUserDetails;
import com.lvt.tmdt.service.intf.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", response));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> response = orderService.getMyOrders(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", response));
    }

}
