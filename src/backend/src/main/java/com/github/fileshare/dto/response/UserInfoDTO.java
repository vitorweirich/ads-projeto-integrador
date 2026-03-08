package com.github.fileshare.dto.response;

import com.github.fileshare.config.entities.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {

	private String name;
	private String email;
	private boolean mfaEnabled;
	private boolean hasAdminPrivileges;
	private StorageDetails storage;
	
	public UserInfoDTO(UserEntity user, boolean hasAdminPrivileges) {
		this.name = user.getName();
		this.email = user.getEmail();
		this.mfaEnabled = user.getMfaVerified();
		this.hasAdminPrivileges = user.getMfaVerified() && hasAdminPrivileges;
	}

}
