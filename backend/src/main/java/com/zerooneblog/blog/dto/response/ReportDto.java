package com.zerooneblog.blog.dto.response;

import java.time.Instant;

import lombok.Data;

@Data
public class ReportDto {
    private Long id;
    private Long reporterId;
    private Long targetUserId;
    private Long targetPostId;
    private com.zerooneblog.blog.dto.response.UserDto reporter;
    private com.zerooneblog.blog.dto.response.UserDto reportedUser;
    private com.zerooneblog.blog.dto.response.PostDto reportedPost;
    private String reason;
    private String status;
    private Instant createdAt;
}
