package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.ShopRegistrationRequest;
import com.lvt.tmdt.dto.response.ShopResponse;
import com.lvt.tmdt.entity.Shop;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.ShopStatus;
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
    
    public Shop mapToEntity(ShopRegistrationRequest request, User user) {
        if (request == null) return null;
        
        return Shop.builder()
                .user(user)
                .shopName(request.getShopName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .description(request.getDescription())
                .status(ShopStatus.PENDING)
                .build();
    }
    
    public void updateEntityFromRequest(Shop shop, ShopRegistrationRequest request) {
        if (request == null || shop == null) return;
        
        shop.setShopName(request.getShopName());
        shop.setPhone(request.getPhone());
        shop.setAddress(request.getAddress());
        shop.setDescription(request.getDescription());
        shop.setStatus(ShopStatus.PENDING);
        shop.setRejectReason(null);
    }
}
