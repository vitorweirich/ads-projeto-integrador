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

import com.github.fileshare.config.entities.UploadedVideoEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.request.ListUsersRequestParams;
import com.github.fileshare.dto.request.ListVideosRequestParams;
import com.github.fileshare.dto.response.AdminVideoSignedUrl;
import com.github.fileshare.dto.response.CompleteUserDTO;
import com.github.fileshare.dto.response.VideoDTO;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.exceptions.VideoNotFoundException;
import com.github.fileshare.mappers.UserMapper;
import com.github.fileshare.mappers.VideoMapper;
import com.github.fileshare.respositories.UploadedVideoRepository;
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
	private final UploadedVideoRepository uploadedVideoRepository;
	private final UserMapper userMapper;
	private final VideoMapper videoMapper;
	private final S3Presigner presigner;
	private final S3Client s3Client;
	
	private static final Integer ADMIN_SHARE_URL_EXPIRATION_IN_MINUTES = 10;
	
	public AdminVideoSignedUrl getVideo(Long videoId) {
		UploadedVideoEntity video = uploadedVideoRepository.findById(videoId)
				.orElseThrow(() -> new VideoNotFoundException("Não foi encontrado video de id: " + videoId));
		
		GetObjectRequest putObjectRequest = GetObjectRequest.builder()
                .bucket(VideoServiceImpl.BUCKET_NAME)
                .key(video.getObjectKey())
                .build();
		
		PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(r -> r.signatureDuration(Duration.ofMinutes(ADMIN_SHARE_URL_EXPIRATION_IN_MINUTES))
        		.getObjectRequest(putObjectRequest));
		
        return PreSignedUrlUtils.extractTimestampsFromPreSignedUrl(
        			AdminVideoSignedUrl.builder().signedUrl(presignedRequest.url().toString()).build()
        		);
	}
	
	public void deleteVideo(Long videoId) {
		UploadedVideoEntity video = uploadedVideoRepository.findById(videoId)
				.orElseThrow(() -> new VideoNotFoundException("Não foi encontrado video de id: " + videoId));
		
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(VideoServiceImpl.BUCKET_NAME)
                .key(video.getObjectKey())
                .build();
        
        try {
        	s3Client.deleteObject(deleteRequest);
		} catch (Exception e) {
			log.warn("AdminService.deleteVideo - error deleting video from storage - videoId {}", videoId, e);
		}
        
        uploadedVideoRepository.delete(video);
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
	
	public Page<VideoDTO> listVideos(ListVideosRequestParams params) {
		PageRequest pageable = PageRequest.of(params.getPage(), params.getRows(), Sort.by(Direction.ASC, "createdAt"));
		
		Page<UploadedVideoEntity> findAll = uploadedVideoRepository.findAll(this.createListVideoSpecification(params), pageable);
		
		ZonedDateTime now = ZonedDateTime.now();
		
		final List<UploadedVideoEntity> videosWithExpiredLink = new ArrayList<>();
		
		List<VideoDTO> content = findAll.getContent().stream().map(video -> {
			if(Objects.nonNull(video.getExpiresIn()) && now.isAfter(video.getExpiresIn())) {
				video.setShareUrl(null);
				video.setExpiresIn(null);
				videosWithExpiredLink.add(video);
			}
			return videoMapper.toDto(video);
		}).toList();
		
		if(!videosWithExpiredLink.isEmpty()) {
			uploadedVideoRepository.saveAll(videosWithExpiredLink);
		}
		
		return new PageImpl<VideoDTO>(content, pageable, findAll.getTotalElements());
	}
	
	private Specification<UploadedVideoEntity> createListVideoSpecification(ListVideosRequestParams params) {
		return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            return cb.and(predicates.toArray(new Predicate[]{}));
        };
	}
	
}
