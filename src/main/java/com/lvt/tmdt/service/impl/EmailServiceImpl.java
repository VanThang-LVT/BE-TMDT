package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.entity.EmailLog;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.mapper.EmailLogMapper;
import com.lvt.tmdt.repository.EmailLogRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.service.intf.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.lvt.tmdt.dto.response.EmailLogResponse;
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailLogMapper emailLogMapper;

    @Override
    public void sendEmail(String to, String subject, String content) {
        User user = userRepository.findByEmail(to).orElse(null);

        EmailLog emailLog = emailLogMapper.toEntity(user, to, subject, content);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@eoviti.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            
            emailLog.setSendStatus("SUCCESS");
        } catch (Exception e) {
            emailLog.setSendStatus("FAILED");
            throw e;
        } finally {
            emailLogRepository.save(emailLog);
        }
    }

    @Override
    public Page<EmailLogResponse> getAllEmailLogs(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        return emailLogRepository.searchEmailLogs(keyword, status, pageable)
                .map(emailLogMapper::mapToResponse);
    }
}
