package com.lvt.tmdt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributeResponse {
    private Integer attrId;
    private String attrName;
    private Boolean isRequired;
}
