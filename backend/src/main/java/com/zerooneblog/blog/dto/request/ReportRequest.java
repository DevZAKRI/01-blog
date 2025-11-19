package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportRequest {
    @NotBlank
    @Size(max = 2000)
    private String reason;
}
