package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.response.ProductResponse;
import com.lvt.tmdt.service.intf.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getActiveProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Short categoryId) {
        List<ProductResponse> responses = productService.getAllActiveProducts(keyword, categoryId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetail(@PathVariable Integer productId) {
        try {
            ProductResponse response = productService.getProductById(productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
