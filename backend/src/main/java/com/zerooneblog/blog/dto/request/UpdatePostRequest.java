package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    // optional media URLs (up to 4 images)
    private String[] mediaUrls;
}
