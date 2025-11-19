package com.zerooneblog.blog.dto.response;

import java.time.Instant;

import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private Long receiverId;
    private String type;
    private String content;
    private boolean isRead;
    private Instant createdAt;
}
