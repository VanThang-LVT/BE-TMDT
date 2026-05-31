package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.CategoryAttributeRequest;
import com.lvt.tmdt.dto.response.CategoryAttributeResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.CategoryAttribute;
import org.springframework.stereotype.Component;

@Component
public class CategoryAttributeMapper {
    public CategoryAttributeResponse mapToResponse(CategoryAttribute attr) {
        if (attr == null)
            return null;
        
        return CategoryAttributeResponse.builder()
                .attrId(attr.getAttrId())
                .attrName(attr.getAttrName())
                .isRequired(attr.getIsRequired())
                .build();
    }
    
    public CategoryAttribute mapToEntity(CategoryAttributeRequest request, Category category) {
        if (request == null)
            return null;
        
        return CategoryAttribute.builder()
                .category(category)
                .attrName(request.getAttrName())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .build();
    }
}
