package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.response.EmailLogResponse;
import com.lvt.tmdt.service.intf.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/email-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmailLogController {

    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<Page<EmailLogResponse>> getAllEmailLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(emailService.getAllEmailLogs(keyword, status, page, size));
    }
}
