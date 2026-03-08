package com.github.fileshare.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RequestPostVideoDTO {

	// Permite apenas nomes de videos, sem a extensão
	@NotBlank(message = "O nome do video não pode estar vazio")
	@Pattern(regexp = "^(?!.*\\.mp4|.*\\.avi|.*\\.mkv|.*\\.x-matroska|.*\\.mov|.*\\.quicktime|.*\\.wmv|.*\\.ts)[^\\/:*?\"<>|]{1,95}$", message = "Deve ser apenas o nome sem extensão! Com até 95 caracteres")
	private String fileName; // (?!.*mp4|.*avi|.*mkv|.*mov|.*wmv|.*ts).*
	
	@NotBlank(message = "O tipo de conteúdo não pode estar vazio")
    @Pattern(regexp = "^video/(mp4|avi|mkv|x-matroska|mov|quicktime||wmv|ts)$", message = "O tipo de conteúdo deve ser 'video/(mp4|avi|mkv|x-matroska|mov|quicktime|wmv|ts)'")
	private String contentType;

	@NotNull
	public Long fileSize;
	
}
