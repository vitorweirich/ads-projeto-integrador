package com.github.fileshare.dto.response;

import java.time.ZonedDateTime;

import com.github.fileshare.specifications.UserSettingsProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteUserDTO {

    private Long id;

    private String name;

    private String email;

    private String role;

    private String mfaSecret;
    
    private Boolean mfaEnabled;
    
    private Long totalSize;

    private ZonedDateTime createdAt;
    
    private UserSettingsProjection settings;

}
