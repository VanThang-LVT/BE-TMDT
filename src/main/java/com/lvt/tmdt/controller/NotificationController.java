package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.ApiResponse;
import com.lvt.tmdt.dto.response.NotificationResponse;
import com.lvt.tmdt.service.intf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<NotificationResponse> data = notificationService.getMyNotifications(email);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông báo thành công", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            long count = notificationService.getUnreadCount(email);
            return ResponseEntity.ok(ApiResponse.success("Thành công", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Integer id) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            notificationService.markAsRead(id, email);
            return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu là đã đọc", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            notificationService.markAllAsRead(email);
            return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả là đã đọc", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
