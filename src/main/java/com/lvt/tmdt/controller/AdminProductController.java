package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.response.ProductResponse;
import com.lvt.tmdt.service.intf.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.getAllProductsForAdmin(keyword));
    }

    @PostMapping("/{productId}/approve")
    public ResponseEntity<?> approveProduct(@PathVariable Integer productId) {
        try {
            ProductResponse response = productService.approveProduct(productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{productId}/reject")
    public ResponseEntity<?> rejectProduct(
            @PathVariable Integer productId,
            @RequestParam(required = false) String reason) {
        try {
            ProductResponse response = productService.rejectProduct(productId, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
