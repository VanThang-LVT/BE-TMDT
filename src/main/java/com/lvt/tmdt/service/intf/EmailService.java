package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.response.EmailLogResponse;
import org.springframework.data.domain.Page;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    
    void sendOrderConfirmationEmail(Long orderId);
    
    Page<EmailLogResponse> getAllEmailLogs(String keyword, String status, int page, int size);
}
