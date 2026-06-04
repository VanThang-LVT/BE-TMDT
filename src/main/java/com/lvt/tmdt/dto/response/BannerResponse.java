package com.lvt.tmdt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {
    private Integer bannerId;
    private String title;
    private String buttonLink;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
