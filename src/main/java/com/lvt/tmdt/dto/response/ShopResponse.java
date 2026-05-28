package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private Integer shopId;
    private String shopName;
    private String phone;
    private String address;
    private String description;
    private String logoUrl;
    private ShopStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private Integer userId;
    private String fullName;
    private String email;
}
