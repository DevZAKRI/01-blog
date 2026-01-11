package com.zerooneblog.blog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 6)
    private String password;

    @Email
    private String email;

    @Size(max = 1000)
    private String bio;

    private String avatar;
}
