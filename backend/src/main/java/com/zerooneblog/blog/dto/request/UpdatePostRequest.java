package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    // optional media URLs (up to 4 images)
    @Size(max = 4, message = "Maximum 4 media files allowed")
    private String[] mediaUrls;
}
