package com.zerooneblog.blog.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private Long receiverId;
    private Long actorId;
    private String type;
    private String content;
    
    @JsonProperty("isRead")
    private boolean isRead;
    
    private Instant createdAt;
}
