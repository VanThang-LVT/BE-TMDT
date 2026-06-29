package com.lvt.tmdt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryResponse {
    private String oldStatus;
    private String newStatus;
    private String updatedByFullName;
    private LocalDateTime createdAt;
}
