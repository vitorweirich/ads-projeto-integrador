package com.github.fileshare.respositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.MagicLinkTokenEntity;

import jakarta.transaction.Transactional;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkTokenEntity, Long> {
    Optional<MagicLinkTokenEntity> findByToken(String token);
    
    @Transactional
	@Modifying
    @Query(value = "DELETE FROM MAGIC_LINK_TOKENS WHERE expires_at < ?1", nativeQuery = true)
    int deleteByExpiresAtDateBefore(Instant now);
}
