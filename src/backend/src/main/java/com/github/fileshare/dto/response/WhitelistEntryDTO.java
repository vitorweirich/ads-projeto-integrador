package com.github.fileshare.dto.response;

import java.time.ZonedDateTime;

import com.github.fileshare.config.entities.EmailWhitelistEntity.Status;

import lombok.Data;

@Data
public class WhitelistEntryDTO {
    private Long id;
    private String email;
    private String invitedName;
    private Status status;
    private ZonedDateTime createdAt;
}
