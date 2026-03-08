package com.github.fileshare.specifications;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithStorageProjection {
	
	private Long id;
	
    private String name;
    
    private String email;
    
    private String mfaSecret;
    
    private Boolean mfaVerified;
    
    private Long totalSize;
    
    private String role;
    
    private ZonedDateTime createdAt;
    
    private UserSettingsProjection settings = new UserSettingsProjection();
    
    public void setSettingStorageLimitBytes(Long storageLimitBytes) {
    	settings.setStorageLimitBytes(storageLimitBytes);
    }
    public void setSettingMaxVideoRetentionDays(Integer maxVideoRetentionDays) {
    	settings.setMaxVideoRetentionDays(maxVideoRetentionDays);
    }
    public void setSettingModifiedAt(ZonedDateTime modifiedAt) {
    	settings.setModifiedAt(modifiedAt);
    }
}

