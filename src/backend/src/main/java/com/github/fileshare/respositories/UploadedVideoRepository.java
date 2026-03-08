package com.github.fileshare.respositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.UploadedVideoEntity;

import jakarta.transaction.Transactional;

@Repository
public interface UploadedVideoRepository extends JpaRepository<UploadedVideoEntity, Long>,
	ListPagingAndSortingRepository<UploadedVideoEntity, Long>, JpaSpecificationExecutor<UploadedVideoEntity> {
	
	@Query("SELECT SUM(v.size) FROM UploadedVideoEntity v WHERE v.user.id = :userId")
	Optional<Long> sumSizeByUserId(@Param("userId") Long userId);

	@Query("SELECT SUM(v.size) FROM UploadedVideoEntity v")
	Optional<Long> sumAllSizes();

	@Transactional
	@Modifying
    @Query(value = "DELETE FROM UPLOADED_VIDEOS WHERE uploaded = false AND created_at < ?1", nativeQuery = true)
    int deleteByNotUploaded(ZonedDateTime now);
	
	List<UploadedVideoEntity> findByCreatedAtBefore(ZonedDateTime dateTime);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE UPLOADED_VIDEOS SET share_url = NULL, short_url_hash = NULL, original_url = NULL, expires_in = NULL WHERE expires_in < :now", nativeQuery = true)
	int updateExpiredLinks(ZonedDateTime now);
	
	Optional<UploadedVideoEntity> findByShortUrlHash(String shortUrlHash);

	@Transactional
	@Modifying
	void deleteByUserId(Long id);
	
}
