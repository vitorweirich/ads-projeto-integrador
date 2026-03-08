package com.github.fileshare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MfaJwtResponse {
    private String token;
    
    @Builder.Default
    private String type = "mfa_token";

}
