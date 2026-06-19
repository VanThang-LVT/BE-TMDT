package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.CommissionConfigRequest;
import com.lvt.tmdt.dto.response.CommissionConfigResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.CommissionConfig;
import org.springframework.stereotype.Component;

@Component
public class CommissionConfigMapper {

    public CommissionConfigResponse mapToResponse(CommissionConfig config) {
        if (config == null) return null;

        return CommissionConfigResponse.builder()
                .commissionId(config.getCommissionId())
                .categoryId(config.getCategory() != null ? config.getCategory().getCategoryId() : null)
                .categoryName(config.getCategory() != null ? config.getCategory().getCategoryName() : null)
                .commissionRate(config.getCommissionRate())
                .status(config.getStatus())
                .createdAt(config.getCreatedAt())
                .build();
    }

    public CommissionConfig mapToEntity(CommissionConfigRequest request, Category category) {
        if (request == null) return null;

        return CommissionConfig.builder()
                .category(category)
                .commissionRate(request.getCommissionRate())
                .status(request.getStatus())
                .build();
    }
}
