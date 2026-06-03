package com.lvt.tmdt.dto.response;

import com.lvt.tmdt.enums.ApprovalStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalHistoryResponse {
    private Integer approvalId;
    private String adminName;
    private ApprovalStatus status;
    private String note;
    private LocalDateTime createdAt;
}
