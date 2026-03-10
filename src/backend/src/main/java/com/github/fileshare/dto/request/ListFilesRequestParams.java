package com.github.fileshare.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListFilesRequestParams extends PaginationRequestParams {
	
	private Boolean uploaded;
	
}
