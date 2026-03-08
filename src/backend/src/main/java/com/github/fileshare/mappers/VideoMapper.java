package com.github.fileshare.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.github.fileshare.config.entities.UploadedVideoEntity;
import com.github.fileshare.dto.response.VideoDTO;

@Mapper(componentModel = "spring")
public interface VideoMapper {

	VideoDTO toDto(UploadedVideoEntity entity);
	
	List<VideoDTO> toDto(List<UploadedVideoEntity> entity);
}
