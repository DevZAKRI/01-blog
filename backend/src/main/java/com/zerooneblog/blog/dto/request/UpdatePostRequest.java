package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @Size(max = 5000)
    private String description;

    private String mediaUrl;
}
