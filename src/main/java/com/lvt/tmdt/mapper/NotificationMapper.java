package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.response.NotificationResponse;
import com.lvt.tmdt.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse mapToResponse(Notification entity) {
        if (entity == null)
            return null;
        return NotificationResponse.builder()
                .notificationId(entity.getNotificationId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
