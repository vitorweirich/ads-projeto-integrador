package com.github.fileshare.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class PaginationRequestParams {

	@PositiveOrZero
	private Integer page = 0;

	@Min(value = 1)
	@Max(value = 100)
	private Integer rows = 10;
	
	@Pattern(regexp = "asc|desc|ASC|DESC")
	private String sort;
	
}
