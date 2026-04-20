package com.github.fileshare.respositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.fileshare.config.entities.EmailWhitelistEntity;
import com.github.fileshare.config.entities.EmailWhitelistEntity.Status;

public interface EmailWhitelistRepository extends JpaRepository<EmailWhitelistEntity, Long> {
    Optional<EmailWhitelistEntity> findByEmail(String email);
    Optional<EmailWhitelistEntity> findByInviteToken(String token);
    Page<EmailWhitelistEntity> findAllByStatus(Status status, Pageable pageable);
}
