package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.response.BannerResponse;
import com.lvt.tmdt.entity.Banner;
import com.lvt.tmdt.repository.BannerRepository;
import com.lvt.tmdt.service.intf.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class PublicBannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerRepository bannerRepository;

    @GetMapping
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getBannerImage(@PathVariable Integer id) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if (banner == null || banner.getImageData() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        String contentType = banner.getContentType();
        if (contentType == null) {
            contentType = "image/jpeg";
        }
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        return new ResponseEntity<>(banner.getImageData(), headers, HttpStatus.OK);
    }
}
