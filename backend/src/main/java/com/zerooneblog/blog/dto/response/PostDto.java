package com.zerooneblog.blog.dto.response;

import java.time.Instant;

import lombok.Data;

@Data
public class PostDto {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String title;
    private String description;
    private String[] mediaUrls;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean hidden;
    private long likesCount;
    private long commentsCount;
    private boolean isLiked;
}
