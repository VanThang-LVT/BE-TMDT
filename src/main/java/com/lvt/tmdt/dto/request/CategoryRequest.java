package com.lvt.tmdt.dto.request;

import com.lvt.tmdt.enums.CategoryStatus;
import lombok.Data;

@Data
public class CategoryRequest {
    private Short parentId;
    private String categoryName;
    private String description;
    private CategoryStatus status;
}
