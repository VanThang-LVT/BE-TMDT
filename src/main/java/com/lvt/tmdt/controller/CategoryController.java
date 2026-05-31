package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.request.CategoryRequest;
import com.lvt.tmdt.dto.request.CategoryAttributeRequest;
import com.lvt.tmdt.dto.response.CategoryResponse;
import com.lvt.tmdt.dto.response.CategoryAttributeResponse;
import com.lvt.tmdt.service.intf.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesForAdmin() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return new ResponseEntity<>(categoryService.createCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Short id, 
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
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
