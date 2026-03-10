package com.github.fileshare.services;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.github.fileshare.config.entities.UploadedFileEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.request.ListUsersRequestParams;
import com.github.fileshare.dto.request.ListFilesRequestParams;
import com.github.fileshare.dto.response.AdminFileSignedUrl;
import com.github.fileshare.dto.response.CompleteUserDTO;
import com.github.fileshare.dto.response.FileDTO;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.exceptions.FileNotFoundException;
import com.github.fileshare.mappers.FileMapper;
import com.github.fileshare.mappers.UserMapper;
import com.github.fileshare.respositories.UploadedFileRepository;
import com.github.fileshare.respositories.UserRepository;
import com.github.fileshare.specifications.UserWithStorageProjection;
import com.github.fileshare.utils.PreSignedUrlUtils;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

	private final UserRepository userRepository;
	private final UploadedFileRepository uploadedFileRepository;
	private final UserMapper userMapper;
	private final FileMapper fileMapper;
	private final S3Presigner presigner;
	private final S3Client s3Client;
	
	private static final Integer ADMIN_SHARE_URL_EXPIRATION_IN_MINUTES = 10;
	
	public AdminFileSignedUrl getFile(Long fileId) {
		UploadedFileEntity file = uploadedFileRepository.findById(fileId)
				.orElseThrow(() -> new FileNotFoundException("Não foi encontrado arquivo de id: " + fileId));
		
		GetObjectRequest putObjectRequest = GetObjectRequest.builder()
                .bucket(FileServiceImpl.BUCKET_NAME)
                .key(file.getObjectKey())
                .build();
		
		PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(r -> r.signatureDuration(Duration.ofMinutes(ADMIN_SHARE_URL_EXPIRATION_IN_MINUTES))
        		.getObjectRequest(putObjectRequest));
		
        return PreSignedUrlUtils.extractTimestampsFromPreSignedUrl(
        			AdminFileSignedUrl.builder().signedUrl(presignedRequest.url().toString()).build()
        		);
	}
	
	public void deleteFile(Long fileId) {
		UploadedFileEntity file = uploadedFileRepository.findById(fileId)
				.orElseThrow(() -> new FileNotFoundException("Não foi encontrado arquivo de id: " + fileId));
		
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(FileServiceImpl.BUCKET_NAME)
                .key(file.getObjectKey())
                .build();
        
        try {
        	s3Client.deleteObject(deleteRequest);
		} catch (Exception e) {
			log.warn("AdminService.deleteFile - error deleting file from storage - fileId {}", fileId, e);
		}
        
        uploadedFileRepository.delete(file);
	}
	
	public Page<CompleteUserDTO> listUsers(ListUsersRequestParams params) {
		Sort sortBy = Sort.by(Direction.fromOptionalString(params.getSort()).orElse(Direction.ASC), "createdAt");
		PageRequest pageable = PageRequest.of(params.getPage(), params.getRows(), sortBy);
		
		Page<UserWithStorageProjection> page = userRepository.findAllWithFiltersAndStorageSum(
				null,
			    null,
			    pageable
			);
        
		return new PageImpl<>(userMapper.projectionToCompleteDTO(page.getContent()), pageable, page.getTotalElements());
    }
	
	public void resetUserMfa(Long userId) {
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o id: " + userId));
		
		user.setMfaVerified(false);
		user.setMfaSecret(null);
		
		userRepository.save(user);
	}
	
	public Page<FileDTO> listFiles(ListFilesRequestParams params) {
		PageRequest pageable = PageRequest.of(params.getPage(), params.getRows(), Sort.by(Direction.ASC, "createdAt"));
		
		Page<UploadedFileEntity> findAll = uploadedFileRepository.findAll(this.createListFileSpecification(params), pageable);
		
		ZonedDateTime now = ZonedDateTime.now();
		
		final List<UploadedFileEntity> filesWithExpiredLink = new ArrayList<>();
		
		List<FileDTO> content = findAll.getContent().stream().map(file -> {
			if(Objects.nonNull(file.getExpiresIn()) && now.isAfter(file.getExpiresIn())) {
				file.setShareUrl(null);
				file.setExpiresIn(null);
				filesWithExpiredLink.add(file);
			}
			return fileMapper.toDto(file);
		}).toList();
		
		if(!filesWithExpiredLink.isEmpty()) {
			uploadedFileRepository.saveAll(filesWithExpiredLink);
		}
		
		return new PageImpl<FileDTO>(content, pageable, findAll.getTotalElements());
	}
	
	private Specification<UploadedFileEntity> createListFileSpecification(ListFilesRequestParams params) {
		return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            return cb.and(predicates.toArray(new Predicate[]{}));
        };
	}
	
}
