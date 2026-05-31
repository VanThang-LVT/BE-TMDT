package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.CategoryRequest;
import com.lvt.tmdt.dto.request.CategoryAttributeRequest;
import com.lvt.tmdt.dto.response.CategoryResponse;
import com.lvt.tmdt.dto.response.CategoryAttributeResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllActiveCategories();
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Short id, CategoryRequest request);
    List<CategoryAttributeResponse> getAttributesByCategory(Short categoryId);
    CategoryAttributeResponse addCategoryAttribute(Short categoryId, CategoryAttributeRequest request);
    CategoryAttributeResponse updateCategoryAttribute(Integer attrId, CategoryAttributeRequest request);
    void deleteCategoryAttribute(Integer attrId);
}
