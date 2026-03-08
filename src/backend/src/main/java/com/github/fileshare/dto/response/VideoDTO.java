package com.github.fileshare.dto.response;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {

	private Long id;
	
	private String name;
	
	private Long size;
	
	private boolean uploaded;
	
	private ZonedDateTime createdAt;
	
	private String shareUrl;
	
	private ZonedDateTime expiresIn;
}
