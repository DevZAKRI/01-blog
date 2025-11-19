package com.zerooneblog.blog.dto.response;

import java.time.Instant;

import lombok.Data;

@Data
public class ReportDto {
    private Long id;
    private Long reporterId;
    private Long targetUserId;
    private String reason;
    private Instant createdAt;
}
