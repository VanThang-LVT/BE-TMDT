package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.BannerRequest;
import com.lvt.tmdt.dto.response.BannerResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface BannerService {
    BannerResponse createBanner(BannerRequest request, MultipartFile image);
    BannerResponse updateBanner(Integer id, BannerRequest request, MultipartFile image);
    void deleteBanner(Integer id);
    List<BannerResponse> getAllBannersForAdmin();
    List<BannerResponse> getActiveBanners();
    BannerResponse toggleBannerStatus(Integer id);
}
