package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.request.BannerRequest;
import com.lvt.tmdt.dto.response.BannerResponse;
import com.lvt.tmdt.service.intf.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<BannerResponse>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBannersForAdmin());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<BannerResponse> createBanner(
            @ModelAttribute BannerRequest request,
            @RequestPart("image") MultipartFile image) {
        return new ResponseEntity<>(bannerService.createBanner(request, image), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<BannerResponse> updateBanner(
            @PathVariable Integer id,
            @ModelAttribute BannerRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(bannerService.updateBanner(id, request, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Integer id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok().body("{\"message\": \"Xóa Banner thành công\"}");
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<BannerResponse> toggleStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(bannerService.toggleBannerStatus(id));
    }
}
