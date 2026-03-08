package com.github.fileshare.config.entities;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "MAGIC_LINK_TOKENS")
@EntityListeners(AuditingEntityListener.class)
public class MagicLinkTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(unique = true)
    private String token;

    @CreatedDate
    private Instant createdAt;

    private Instant expiresAt;

    // optional: registrar IP ou User-Agent
}
