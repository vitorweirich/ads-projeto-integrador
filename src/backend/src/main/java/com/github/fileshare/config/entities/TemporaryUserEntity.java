package com.github.fileshare.config.entities;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "TEMPORARY_USERS")
@EntityListeners(AuditingEntityListener.class)
public class TemporaryUserEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String confirmationToken;
    
    private ZonedDateTime expiresAt;
    
    @CreatedDate
	private ZonedDateTime createdAt;
    
}
