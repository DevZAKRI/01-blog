package com.zerooneblog.blog.dto.response;

import java.time.Instant;
import java.util.Set;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private boolean banned;
    private Instant createdAt;
    private String role;
    private Set<Long> subscriberIds;
    private Set<Long> subscriptionIds;
}
