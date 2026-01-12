package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportRequest {
    @NotBlank(message = "Report reason is required")
    @Size(max = 2000, message = "Report reason cannot exceed 2000 characters")
    private String reason;
}
