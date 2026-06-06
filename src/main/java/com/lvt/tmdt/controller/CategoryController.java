package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.request.CategoryRequest;
import com.lvt.tmdt.dto.request.CategoryAttributeRequest;
import com.lvt.tmdt.dto.response.CategoryResponse;
import com.lvt.tmdt.dto.response.CategoryAttributeResponse;
import com.lvt.tmdt.service.intf.CategoryService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.lvt.tmdt.repository.CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(categoryService.getAllActiveCategories(keyword));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesForAdmin(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(categoryService.getAllCategories(keyword));
    }

    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws java.io.IOException {
        CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
        return new ResponseEntity<>(categoryService.createCategory(request, image), HttpStatus.CREATED);
    }

    @PutMapping(value = "/admin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Short id, 
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws java.io.IOException {
        CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
        return ResponseEntity.ok(categoryService.updateCategory(id, request, image));
    }

    @GetMapping("/public/{id}/image")
    public ResponseEntity<byte[]> getCategoryImage(@PathVariable Short id) {
        return categoryRepository.findById(id)
                .filter(cat -> cat.getImageData() != null)
                .map(cat -> ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(cat.getContentType() != null ? cat.getContentType() : "image/jpeg"))
                        .body(cat.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<CategoryAttributeResponse>> getCategoryAttributes(@PathVariable Short id) {
        return ResponseEntity.ok(categoryService.getAttributesByCategory(id));
    }

    @PostMapping("/admin/{id}/attributes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryAttributeResponse> addCategoryAttribute(
            @PathVariable Short id,
            @RequestBody CategoryAttributeRequest request) {
        return new ResponseEntity<>(categoryService.addCategoryAttribute(id, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/attributes/{attrId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategoryAttribute(@PathVariable Integer attrId) {
        categoryService.deleteCategoryAttribute(attrId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/attributes/{attrId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryAttributeResponse> updateCategoryAttribute(
            @PathVariable Integer attrId,
            @RequestBody CategoryAttributeRequest request) {
        return ResponseEntity.ok(categoryService.updateCategoryAttribute(attrId, request));
    }
}
