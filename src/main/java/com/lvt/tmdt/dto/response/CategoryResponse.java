package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.CategoryStatus;
import lombok.Data;

@Data
public class CategoryResponse {
    private Short categoryId;
    private Short parentId;
    private String categoryName;
    private String description;
    private CategoryStatus status;
    private boolean hasImage;
}
