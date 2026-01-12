package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String text;
}
