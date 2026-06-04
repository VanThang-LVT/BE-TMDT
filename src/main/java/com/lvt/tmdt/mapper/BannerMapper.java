package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.BannerRequest;
import com.lvt.tmdt.dto.response.BannerResponse;
import com.lvt.tmdt.entity.Banner;
import org.springframework.stereotype.Component;

@Component
public class BannerMapper {

    public BannerResponse mapToResponse(Banner banner) {
        if (banner == null) return null;
        
        return BannerResponse.builder()
                .bannerId(banner.getBannerId())
                .title(banner.getTitle())
                .buttonLink(banner.getButtonLink())
                .isActive(banner.getIsActive())
                .displayOrder(banner.getDisplayOrder())
                .createdAt(banner.getCreatedAt())
                .build();
    }

    public Banner mapToEntity(BannerRequest request, org.springframework.web.multipart.MultipartFile image) throws java.io.IOException {
        if (request == null) return null;
        
        return Banner.builder()
                .title(request.getTitle())
                .imageData(image != null && !image.isEmpty() ? image.getBytes() : null)
                .contentType(image != null && !image.isEmpty() ? image.getContentType() : null)
                .buttonLink(request.getButtonLink())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
    }

    public void updateEntityFromRequest(Banner banner, BannerRequest request) {
        if (request == null || banner == null) return;
        
        if (request.getTitle() != null) banner.setTitle(request.getTitle());
        if (request.getButtonLink() != null) banner.setButtonLink(request.getButtonLink());
        if (request.getIsActive() != null) banner.setIsActive(request.getIsActive());
        if (request.getDisplayOrder() != null) banner.setDisplayOrder(request.getDisplayOrder());
    }
}
