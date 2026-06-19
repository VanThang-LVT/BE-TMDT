package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.CommissionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CommissionConfigResponse {
    private Integer commissionId;
    private Short categoryId;
    private String categoryName;
    private BigDecimal commissionRate;
    private CommissionStatus status;
    private LocalDateTime createdAt;
}
