package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.ApiResponse;
import com.lvt.tmdt.dto.request.ShopApprovalRequest;
import com.lvt.tmdt.dto.request.ShopRegistrationRequest;
import com.lvt.tmdt.dto.response.ShopResponse;
import com.lvt.tmdt.enums.ShopStatus;
import com.lvt.tmdt.service.intf.ShopService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.lvt.tmdt.sercurity.CustomUserDetails;
import com.lvt.tmdt.service.intf.OrderService;
import com.lvt.tmdt.dto.response.ShopOrderResponse;
import com.lvt.tmdt.enums.ShopOrderStatus;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ShopResponse>> registerShop(@Valid @RequestBody ShopRegistrationRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            ShopResponse response = shopService.registerShop(email, request);
            return ResponseEntity.ok(ApiResponse.<ShopResponse>builder()
                    .success(true)
                    .message("Gửi yêu cầu đăng ký gian hàng thành công")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ShopResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/my-shop")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ShopResponse>> getMyShop() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            ShopResponse response = shopService.getMyShop(email);
            return ResponseEntity.ok(ApiResponse.<ShopResponse>builder()
                    .success(true)
                    .message("Lấy thông tin thành công")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ShopResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getAllShops(
            @RequestParam(required = false) ShopStatus status) {
        try {
            List<ShopResponse> response = shopService.getAllShops(status);
            return ResponseEntity.ok(ApiResponse.<List<ShopResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách thành công")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<ShopResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PutMapping("/admin/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopResponse>> approveOrRejectShop(
            @PathVariable Integer id,
            @Valid @RequestBody ShopApprovalRequest request) {
        try {
            ShopResponse response = shopService.approveOrRejectShop(id, request);
            String action = request.getStatus() == ShopStatus.ACTIVE ? "Duyệt" : "Từ chối/Khóa";
            return ResponseEntity.ok(ApiResponse.<ShopResponse>builder()
                    .success(true)
                    .message(action + " gian hàng thành công")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ShopResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Page<ShopOrderResponse>>> getShopOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<ShopOrderStatus> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
            
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ShopOrderResponse> response = orderService.getSellerOrders(userDetails.getUserId(), keyword, statuses, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ShopOrderResponse>>builder()
                .success(true)
                .message("Lấy danh sách đơn hàng của shop thành công")
                .data(response)
                .build());
    }

    @GetMapping("/orders/counts")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOrderCountsByStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Long> counts = orderService.getOrderCountsByStatus(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.<Map<String, Long>>builder()
                .success(true)
                .message("Lấy số lượng đơn hàng theo trạng thái thành công")
                .data(counts)
                .build());
    }

    @PutMapping("/orders/{shopOrderId}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ShopOrderResponse>> updateShopOrderStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopOrderId,
            @RequestParam ShopOrderStatus status,
            @RequestParam(required = false) String cancelReason) {
        ShopOrderResponse response = orderService.updateShopOrderStatus(userDetails.getUserId(), shopOrderId, status, cancelReason);
        return ResponseEntity.ok(ApiResponse.<ShopOrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(response)
                .build());
    }
}
