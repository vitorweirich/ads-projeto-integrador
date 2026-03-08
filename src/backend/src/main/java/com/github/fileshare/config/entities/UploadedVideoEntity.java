package com.github.fileshare.config.entities;

import java.time.ZonedDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "UPLOADED_VIDEOS")
@EntityListeners(AuditingEntityListener.class)
public class UploadedVideoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private String objectKey;
	
	@Column(name = "size_bytes")
	private Long size;
	
	private boolean uploaded;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
	private UserEntity user;
	
	@CreatedDate
	private ZonedDateTime createdAt;
	
	private String shareUrl;
	
	private String originalUrl;

	private String shortUrlHash;
	
	private ZonedDateTime expiresIn;
}
