package com.github.fileshare.dto.response;

import com.github.fileshare.validation.ContentTypeAllowed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RequestPostFileDTO {

	// Permite apenas nomes de arquivos, sem a extensão
	@NotBlank(message = "O nome do arquivo não pode estar vazio")
	@Pattern(regexp = "^[^\\/:*?\"<>|.]{1,70}$", message = "Deve ser apenas o nome do arquivo, sem a extensão, com até 70 caracteres")
	private String fileName;
	
	@NotBlank(message = "O tipo de conteúdo não pode estar vazio")
	@ContentTypeAllowed
	private String contentType;

	@NotNull
	public Long fileSize;
	
}
