package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.ApiResponse;
import com.lvt.tmdt.dto.request.CommissionConfigRequest;
import com.lvt.tmdt.dto.response.CommissionConfigResponse;
import com.lvt.tmdt.service.intf.CommissionConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/commissions")
@RequiredArgsConstructor
public class CommissionConfigController {

    private final CommissionConfigService commissionConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommissionConfigResponse>>> getAllConfigs() {
        List<CommissionConfigResponse> configs = commissionConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cấu hình hoa hồng thành công", configs));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommissionConfigResponse>> createConfig(@Valid @RequestBody CommissionConfigRequest request) {
        CommissionConfigResponse config = commissionConfigService.createConfig(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo cấu hình hoa hồng thành công", config));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionConfigResponse>> updateConfig(@PathVariable Integer id, @Valid @RequestBody CommissionConfigRequest request) {
        CommissionConfigResponse config = commissionConfigService.updateConfig(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình hoa hồng thành công", config));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CommissionConfigResponse>> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        CommissionConfigResponse config = commissionConfigService.updateConfigStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", config));
    }
}
