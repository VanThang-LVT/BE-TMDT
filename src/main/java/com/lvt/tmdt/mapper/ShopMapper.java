package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.response.ShopResponse;
import com.lvt.tmdt.entity.Shop;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {
    public ShopResponse mapToResponse(Shop entity) {
        if (entity == null)
            return null;
        return ShopResponse.builder()
                .shopId(entity.getShopId())
                .shopName(entity.getShopName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .status(entity.getStatus())
                .rejectReason(entity.getRejectReason())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .fullName(entity.getUser() != null ? entity.getUser().getFullName() : null)
                .email(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .build();
    }
}
