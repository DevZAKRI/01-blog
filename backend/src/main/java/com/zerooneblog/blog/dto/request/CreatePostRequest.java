package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {
    @NotBlank
    @Size(max = 5000)
    private String description;

    // optional media URL returned by upload endpoint
    private String mediaUrl;
}
