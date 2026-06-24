package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.response.EmailLogResponse;
import com.lvt.tmdt.entity.EmailLog;
import com.lvt.tmdt.entity.User;
import org.springframework.stereotype.Component;

@Component
public class EmailLogMapper {
    public EmailLogResponse mapToResponse(EmailLog entity) {
        if (entity == null) {
            return null;
        }
        return EmailLogResponse.builder()
                .emailId(entity.getEmailId())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .orderId(entity.getOrder() != null ? entity.getOrder().getOrderId() : null)
                .recipientEmail(entity.getRecipientEmail())
                .subject(entity.getSubject())
                .content(entity.getContent())
                .sendStatus(entity.getSendStatus())
                .sentAt(entity.getSentAt())
                .build();
    }

    public EmailLog toEntity(User user, String recipientEmail, String subject, String content) {
        return EmailLog.builder()
                .user(user)
                .recipientEmail(recipientEmail)
                .subject(subject)
                .content(content)
                .build();
    }
}
