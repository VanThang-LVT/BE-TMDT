package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.BannerRequest;
import com.lvt.tmdt.dto.response.BannerResponse;
import com.lvt.tmdt.entity.Banner;
import com.lvt.tmdt.repository.BannerRepository;
import com.lvt.tmdt.service.intf.BannerService;
import com.lvt.tmdt.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerRepository bannerRepository;
    
    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public BannerResponse createBanner(BannerRequest request, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ảnh cho Banner");
        }

        try {
            Banner banner = bannerMapper.mapToEntity(request, image);

            Banner saved = bannerRepository.save(banner);
            return bannerMapper.mapToResponse(saved);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file ảnh: " + e.getMessage());
        }
    }

    @Override
    public BannerResponse updateBanner(Integer id, BannerRequest request, MultipartFile image) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Banner"));

        bannerMapper.updateEntityFromRequest(banner, request);

        if (image != null && !image.isEmpty()) {
            try {
                banner.setImageData(image.getBytes());
                banner.setContentType(image.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi đọc file ảnh mới: " + e.getMessage());
            }
        }

        Banner updated = bannerRepository.save(banner);
        return bannerMapper.mapToResponse(updated);
    }

    @Override
    public void deleteBanner(Integer id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Banner"));
        bannerRepository.delete(banner);
    }

    @Override
    public List<BannerResponse> getAllBannersForAdmin() {
        return bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc().stream()
                .map(bannerMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BannerResponse> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc().stream()
                .map(bannerMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BannerResponse toggleBannerStatus(Integer id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Banner"));
        banner.setIsActive(!banner.getIsActive());
        return bannerMapper.mapToResponse(bannerRepository.save(banner));
    }
}
