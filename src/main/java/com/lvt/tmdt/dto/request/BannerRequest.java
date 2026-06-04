package com.lvt.tmdt.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BannerRequest {
    private String title;
    private String buttonLink;
    private Boolean isActive;
    private Integer displayOrder;
}
