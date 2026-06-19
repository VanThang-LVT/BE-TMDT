package com.lvt.tmdt.dto.request;

import com.lvt.tmdt.enums.CommissionStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionConfigRequest {
    @NotNull(message = "Vui lòng chọn danh mục (Thể loại)")
    private Short categoryId;

    @NotNull(message = "Vui lòng nhập tỉ lệ hoa hồng")
    @DecimalMin(value = "0.0", message = "Tỉ lệ hoa hồng không được nhỏ hơn 0")
    @DecimalMax(value = "1.0", message = "Tỉ lệ hoa hồng không được vượt quá 100%")
    private BigDecimal commissionRate;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private CommissionStatus status;
}
