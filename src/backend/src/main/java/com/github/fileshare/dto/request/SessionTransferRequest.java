package com.github.fileshare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SessionTransferRequest {

	@NotBlank
	@Pattern(regexp = "WEB|MOBILE")
	private String target;
}
