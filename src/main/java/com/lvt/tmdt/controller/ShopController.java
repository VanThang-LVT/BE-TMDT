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

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShopController {

    @Autowired
    private ShopService shopService;

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
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getAllShops(@RequestParam(required = false) ShopStatus status) {
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
}
