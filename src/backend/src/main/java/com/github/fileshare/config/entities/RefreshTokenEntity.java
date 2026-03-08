package com.github.fileshare.config.entities;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "REFRESH_TOKENS")
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenEntity {

    @Id
    private UUID id;

    private Long userId;

    private String familyId;

    private Instant createdAt;

    private Instant expiresAt;

    private boolean revoked;

    private String replacedBy;

    private String userAgent;

    private String ipAddress;

    public RefreshTokenEntity() {
        this.id = UUID.randomUUID();
    }

}
