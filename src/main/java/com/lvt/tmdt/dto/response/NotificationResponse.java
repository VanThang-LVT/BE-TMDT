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
public class NotificationResponse {
    private Integer notificationId;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
