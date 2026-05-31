package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.CategoryRequest;
import com.lvt.tmdt.dto.response.CategoryResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.enums.CategoryStatus;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse mapToResponse(Category category) {
        if (category == null)
            return null;
        
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setParentId(category.getParentId());
        response.setCategoryName(category.getCategoryName());
        response.setDescription(category.getDescription());
        response.setStatus(category.getStatus());
        return response;
    }

    public Category mapToEntity(CategoryRequest request) {
        if (request == null)
            return null;
        
        return Category.builder()
                .parentId(request.getParentId())
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : CategoryStatus.ACTIVE)
                .build();
    }
    
    public void updateEntityFromRequest(Category category, CategoryRequest request) {
        if (request == null || category == null)
            return;
        
        category.setParentId(request.getParentId());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
    }
}
