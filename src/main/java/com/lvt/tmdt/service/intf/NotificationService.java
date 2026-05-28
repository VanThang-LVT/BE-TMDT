package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.response.NotificationResponse;
import com.lvt.tmdt.entity.User;

import java.util.List;

public interface NotificationService {
    void createNotification(User user, String title, String content);
    void sendToAllAdmins(String title, String content);
    List<NotificationResponse> getMyNotifications(String email);
    long getUnreadCount(String email);
    void markAsRead(Integer notificationId, String email);
    void markAllAsRead(String email);
}
