package com.github.fileshare.respositories;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.RefreshTokenEntity;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    List<RefreshTokenEntity> findByFamilyId(String familyId);
    
    @Transactional
	@Modifying
    @Query(value = "DELETE FROM REFRESH_TOKENS WHERE expires_at < ?1", nativeQuery = true)
    int deleteByExpiresAtDateBefore(Instant now);
    
    @Transactional
	@Modifying
	@Query(value = "UPDATE REFRESH_TOKENS SET revoked = true WHERE family_id = :familyId", nativeQuery = true)
	int revokeUserTokensByFamilyId(String familyId);
    
    @Transactional
	@Modifying
	@Query(value = "UPDATE REFRESH_TOKENS SET revoked = true WHERE user_id = :userId", nativeQuery = true)
	int revokeAllUserTokensByUserId(Long userId);
}
