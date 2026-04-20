package com.github.fileshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteRequest {
    @NotBlank
    @Email
    private String email;

    private String name;
}
