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

import com.github.fileshare.config.entities.UploadedFileEntity;

import jakarta.transaction.Transactional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFileEntity, Long>,
	ListPagingAndSortingRepository<UploadedFileEntity, Long>, JpaSpecificationExecutor<UploadedFileEntity> {
	
	@Query("SELECT SUM(v.size) FROM UploadedFileEntity v WHERE v.user.id = :userId")
	Optional<Long> sumSizeByUserId(@Param("userId") Long userId);

	@Query("SELECT SUM(v.size) FROM UploadedFileEntity v")
	Optional<Long> sumAllSizes();

	@Transactional
	@Modifying
    @Query(value = "DELETE FROM UPLOADED_FILES WHERE uploaded = false AND created_at < ?1", nativeQuery = true)
    int deleteByNotUploaded(ZonedDateTime now);
	
	List<UploadedFileEntity> findByCreatedAtBefore(ZonedDateTime dateTime);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE UPLOADED_FILES SET share_url = NULL, short_url_hash = NULL, original_url = NULL, expires_in = NULL WHERE expires_in < :now", nativeQuery = true)
	int updateExpiredLinks(ZonedDateTime now);
	
	Optional<UploadedFileEntity> findByShortUrlHash(String shortUrlHash);

	@Transactional
	@Modifying
	void deleteByUserId(Long id);
	
}
