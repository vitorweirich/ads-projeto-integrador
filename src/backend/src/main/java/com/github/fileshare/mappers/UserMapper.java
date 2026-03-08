package com.github.fileshare.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.response.CompleteUserDTO;
import com.github.fileshare.specifications.UserWithStorageProjection;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "mfaSecret", qualifiedByName = "applyMaskToSecret")
	@Mapping(source = "mfaVerified", target = "mfaEnabled")
	CompleteUserDTO toCompleteDTO(UserEntity entity);
	
	List<CompleteUserDTO> toCompleteDTO(List<UserEntity> entity);

	@Mapping(target = "mfaSecret", qualifiedByName = "applyMaskToSecret")
	@Mapping(source = "mfaVerified", target = "mfaEnabled")
	CompleteUserDTO projectionToCompleteDTO(UserWithStorageProjection projection);
	
	List<CompleteUserDTO> projectionToCompleteDTO(List<UserWithStorageProjection> entity);
	
	@Named("applyMaskToSecret")
    default String applyMaskToSecret(String secret) {
		if(!StringUtils.hasLength(secret)) {
			return null;
		}
		
        return "*".repeat(secret.length());
    }
}
