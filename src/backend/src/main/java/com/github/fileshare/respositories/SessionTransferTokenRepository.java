package com.github.fileshare.respositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.SessionTransferTokenEntity;

@Repository
public interface SessionTransferTokenRepository extends JpaRepository<SessionTransferTokenEntity, UUID> {
}

