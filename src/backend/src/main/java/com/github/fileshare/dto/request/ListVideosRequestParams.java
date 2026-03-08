package com.github.fileshare.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListVideosRequestParams extends PaginationRequestParams {
	
	private Boolean uploaded;
	
}
