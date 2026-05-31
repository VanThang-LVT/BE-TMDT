package com.lvt.tmdt.dto.request;

import lombok.Data;

@Data
public class CategoryAttributeRequest {
    private String attrName;
    private Boolean isRequired;
}
