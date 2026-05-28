package com.lvt.tmdt.dto.request;

import com.lvt.tmdt.enums.ShopStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopApprovalRequest {
    @NotNull(message = "Trạng thái duyệt không được để trống")
    private ShopStatus status;
    
    private String reason;
}
