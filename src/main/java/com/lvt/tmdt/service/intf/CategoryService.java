package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.CategoryRequest;
import com.lvt.tmdt.dto.request.CategoryAttributeRequest;
import com.lvt.tmdt.dto.response.CategoryResponse;
import com.lvt.tmdt.dto.response.CategoryAttributeResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllActiveCategories(String keyword);
    List<CategoryResponse> getAllCategories(String keyword);
    CategoryResponse createCategory(CategoryRequest request, MultipartFile image);
    CategoryResponse updateCategory(Short id, CategoryRequest request, MultipartFile image);
    List<CategoryAttributeResponse> getAttributesByCategory(Short categoryId);
    CategoryAttributeResponse addCategoryAttribute(Short categoryId, CategoryAttributeRequest request);
    CategoryAttributeResponse updateCategoryAttribute(Integer attrId, CategoryAttributeRequest request);
    void deleteCategoryAttribute(Integer attrId);
}
