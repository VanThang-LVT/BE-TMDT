package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.request.ProductRequest;
import com.lvt.tmdt.dto.response.ProductResponse;
import com.lvt.tmdt.sercurity.CustomUserDetails;
import com.lvt.tmdt.service.intf.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProductResponse response = productService.createProduct(request, userDetails.getUserId(), images);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<ProductResponse>> getMyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProductResponse> responses = productService.getProductsBySeller(userDetails.getUserId());
        return ResponseEntity.ok(responses);
    }

    @PutMapping(value = "/{productId}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer productId,
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            ProductResponse response = productService.updateProduct(productId, request, userDetails.getUserId(), images);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            productService.deleteProduct(productId, userDetails.getUserId());
            return ResponseEntity.ok().body("{\"message\": \"Xóa sản phẩm thành công\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
