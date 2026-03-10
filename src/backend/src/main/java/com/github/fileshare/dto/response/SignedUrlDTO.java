package com.github.fileshare.dto.response;

import java.time.ZonedDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignedUrlDTO {

	private String signedUrl;
	
	private Map<String, Object> metadata;
	
	private Long fileId;
	
	private ZonedDateTime expirationDate;

}
