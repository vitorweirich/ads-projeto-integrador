package com.github.fileshare.specifications;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsProjection {
	
	private Long storageLimitBytes;

    private Integer maxVideoRetentionDays;

    private ZonedDateTime modifiedAt;
}

