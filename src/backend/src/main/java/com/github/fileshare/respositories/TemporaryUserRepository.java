package com.github.fileshare.respositories;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.github.fileshare.config.entities.TemporaryUserEntity;

import jakarta.transaction.Transactional;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUserEntity, Long> {
    boolean existsByEmail(String email);
    Optional<TemporaryUserEntity> findByConfirmationToken(String token);
    
    Optional<TemporaryUserEntity> findByEmail(String email);
    
    @Transactional
	@Modifying
    @Query(value = "DELETE FROM TEMPORARY_USERS WHERE expires_at < ?1", nativeQuery = true)
    int deleteByExpiresInDateBefore(ZonedDateTime now);
}
