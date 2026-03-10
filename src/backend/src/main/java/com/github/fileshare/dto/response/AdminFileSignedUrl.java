package com.github.fileshare.dto.response;

import com.github.fileshare.utils.PreSignedUrlUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminFileSignedUrl {
	
	private String signedUrl;
	
	private PreSignedUrlUtils.UrlTimestamps timestamps;

}
