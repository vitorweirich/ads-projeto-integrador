package com.github.fileshare.dto.response;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkDTO {

	private String id;
    
    private String originalUrl;
    
    private String shortUrl;
    
    private ZonedDateTime expiresIn;
}
