package com.zerooneblog.blog.dto.response;

import java.time.Instant;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long postId;
    private String text;
    private Instant createdAt;
}
