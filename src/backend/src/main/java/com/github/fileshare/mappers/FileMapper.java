package com.github.fileshare.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.github.fileshare.config.entities.UploadedFileEntity;
import com.github.fileshare.dto.response.FileDTO;

@Mapper(componentModel = "spring")
public interface FileMapper {

	FileDTO toDto(UploadedFileEntity entity);
    
	List<FileDTO> toDto(List<UploadedFileEntity> entity);
}
