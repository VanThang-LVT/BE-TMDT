package com.lvt.tmdt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLogResponse {
    private Long emailId;
    private Integer userId;
    private Long orderId;
    private String recipientEmail;
    private String subject;
    private String content;
    private String sendStatus;
    private LocalDateTime sentAt;
}
