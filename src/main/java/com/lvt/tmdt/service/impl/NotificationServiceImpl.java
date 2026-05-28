package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.response.NotificationResponse;
import com.lvt.tmdt.entity.Notification;
import com.lvt.tmdt.entity.Role;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.repository.NotificationRepository;
import com.lvt.tmdt.repository.RoleRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.service.intf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public void createNotification(User user, String title, String content) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void sendToAllAdmins(String title, String content) {
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
        if (adminRole != null) {
            List<User> admins = userRepository.findByRolesContaining(adminRole);
            for (User admin : admins) {
                createNotification(admin, title, content);
            }
        }
    }

    @Override
    public List<NotificationResponse> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        
        if (!notification.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền cập nhật thông báo này");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
                
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification notification : notifications) {
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    private NotificationResponse mapToResponse(Notification entity) {
        return NotificationResponse.builder()
                .notificationId(entity.getNotificationId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
