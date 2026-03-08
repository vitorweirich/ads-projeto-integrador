package com.github.fileshare.services;

import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.slugify.Slugify;
import com.github.fileshare.config.StorageProperties;
import com.github.fileshare.config.entities.UploadedVideoEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.config.entities.UserSettingsEntity;
import com.github.fileshare.dto.internal.SimplifiedUser;
import com.github.fileshare.dto.request.ListVideosRequestParams;
import com.github.fileshare.dto.response.RequestPostVideoDTO;
import com.github.fileshare.dto.response.ShortLinkDTO;
import com.github.fileshare.dto.response.SignedUrlDTO;
import com.github.fileshare.dto.response.VideoDTO;
import com.github.fileshare.exceptions.MessageFeedbackException;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.exceptions.VideoNotFoundException;
import com.github.fileshare.mappers.VideoMapper;
import com.github.fileshare.respositories.UploadedVideoRepository;
import com.github.fileshare.respositories.UserSettingsRepository;
import com.github.fileshare.utils.AuthenticatedUserUtils;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

	private final S3Presigner presigner;
    private final UploadedVideoRepository uploadedVideoRepository;
    private final LinkService linkService;
    private final VideoMapper videoMapper;
    private final StorageProperties storageProperties;
    private final UserSettingsRepository userSettingsRepository;
    private final S3Client s3Client;
    private final Slugify slugify = Slugify.builder().build();

    public static final String BUCKET_NAME = "videos";
    private static final Integer SHARE_URL_EXPIRATION_IN_MINUTES = 120;
    public static final Integer PUT_VIDEO_URL_EXPIRATION_IN_MINUTES = 60;
    private static final Integer SHARE_URL_TOLERATION_IN_MINUTES = 30;

    // -------------------------
    // Métodos auxiliares
    // -------------------------

    /**
     * Calcula o uso total de armazenamento por um usuário.
     */
    private long getUserStorageUsage(Long userId) {
        return uploadedVideoRepository.sumSizeByUserId(userId).orElse(0L);
    }

    /**
     * Calcula o uso total de armazenamento no sistema.
     */
    private long getTotalStorageUsage() {
        return uploadedVideoRepository.sumAllSizes().orElse(0L);
    }

    /**
     * Verifica se há espaço disponível para upload de determinado tamanho.
     */
    private void checkStorageLimits(Long userId, long newFileSize) {
    	UserSettingsEntity userSettings = userSettingsRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Unable to retrieve user settings!"));
    	Long userStorageLimitBytes = userSettings.getStorageLimitBytes();
    	
        long currentUserUsage = getUserStorageUsage(userId);
        long currentTotalUsage = getTotalStorageUsage();

        if (currentUserUsage + newFileSize > userStorageLimitBytes) {
            throw new MessageFeedbackException("Limite de armazenamento por usuário excedido. Tente excluir alguns videos ou aguarde que eles expirem automaticamente!");
        }

        if (currentTotalUsage + newFileSize > storageProperties.getMaxTotal()) {
            throw new MessageFeedbackException("Limite total de armazenamento do sistema excedido. Aguarde algumas horas e tente novamente");
        }
    }
	
	@Override
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
	
	@Override
	@Transactional
	public void deleteVideo(Long videoId) {
		UploadedVideoEntity video = uploadedVideoRepository.findById(videoId)
				.orElseThrow(() -> new VideoNotFoundException("Não foi encontrado video de id: " + videoId));
		
        SimplifiedUser simplifiedUser = AuthenticatedUserUtils.requireSimplifiedUser();
        if(!simplifiedUser.getEmail().equals(video.getUser().getEmail())) {
        	throw new AccessDeniedException("Você não tem permissão para remover esse vídeo!");
        }
		
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(VideoServiceImpl.BUCKET_NAME)
                .key(video.getObjectKey())
                .build();
        
        try {
        	s3Client.deleteObject(deleteRequest);
		} catch (Exception e) {
			log.warn("VideoServiceImpl.deleteVideo - error deleting video from storage - videoId {}", videoId, e);
		}
        
        uploadedVideoRepository.delete(video);
	}
	
	private Specification<UploadedVideoEntity> createListVideoSpecification(ListVideosRequestParams params) {
		return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            SimplifiedUser simplifiedUser = AuthenticatedUserUtils.requireSimplifiedUser();
            
            predicates.add(cb.equal(root.get("user").get("email"), simplifiedUser.getEmail()));
            
            return cb.and(predicates.toArray(new Predicate[]{}));
        };
	}
	
	@Override
	@Transactional
	public SignedUrlDTO requestSignedUrlToGetVideo(Long videoId) {
        try {
        	UploadedVideoEntity uploadedVideoById = this.getUploadedVideoById(videoId);
        	
        	SimplifiedUser user = AuthenticatedUserUtils.requireSimplifiedUser();
            if(!user.getEmail().equals(uploadedVideoById.getUser().getEmail())) {
            	throw new AccessDeniedException("Você não tem permissão para requisitar esse vídeo!");
            }
        	
        	if(!uploadedVideoById.isUploaded()) {
        		throw new MessageFeedbackException("O upload do video com id [%s] não foi finalizado!".formatted(videoId));
        	}
        	
        	ZonedDateTime now = ZonedDateTime.now();
        	
        	Map<String, Object> metadata = Map.of(
        			"videoName", uploadedVideoById.getName(),
        			"creatorName", user.getName(),
        			"creatorEmail", user.getEmail()
        			);
        	
        	if(StringUtils.hasText(uploadedVideoById.getShareUrl()) && uploadedVideoById.getExpiresIn()
        			.isAfter(now.plusMinutes(SHARE_URL_TOLERATION_IN_MINUTES))) {
        		
        		return SignedUrlDTO.builder()
        				.signedUrl(uploadedVideoById.getShareUrl())
        				.metadata(metadata)
        				.videoId(videoId)
        				.expirationDate(uploadedVideoById.getExpiresIn())
        				.build();
        	}
        	
        	GetObjectRequest putObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(uploadedVideoById.getObjectKey())
                    .build();
        	
        	
        	// TODO: Calcular quanto tempo o video ainda estará disponível com base na data de criação
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(r -> r.signatureDuration(Duration.ofMinutes(SHARE_URL_EXPIRATION_IN_MINUTES))
            		.getObjectRequest(putObjectRequest));

            URL signedUri = presignedRequest.url();
            
            String signedUrl = signedUri.toString();
            
            ZonedDateTime expiresIn = now.plusMinutes(SHARE_URL_EXPIRATION_IN_MINUTES);
            
            ShortLinkDTO shortenUrl = linkService.shortenUrl(signedUrl, expiresIn, uploadedVideoById);
            
            uploadedVideoById.setShareUrl(shortenUrl.getShortUrl());
            uploadedVideoById.setExpiresIn(expiresIn);
            uploadedVideoById.setOriginalUrl(shortenUrl.getOriginalUrl());
            uploadedVideoById.setShortUrlHash(shortenUrl.getId());
            
            uploadedVideoRepository.save(uploadedVideoById);
            
            return SignedUrlDTO.builder()
            		.signedUrl(shortenUrl.getShortUrl())
            		.metadata(metadata)
            		.videoId(videoId)
            		.expirationDate(uploadedVideoById.getExpiresIn())
            		.build();
        } catch (SdkClientException e) {
            log.error("VideoServiceImpl.requestSignedUrlToGetVideo - error - videoId {}", videoId, e);
            throw e;
        }
	}
	
	@Override
    public SignedUrlDTO requestSignedUrlToPostVideo(RequestPostVideoDTO body) {
        String videoName = body.getFileName();

        // Aqui assumimos que o tamanho do arquivo foi informado no DTO (bytes)
        long estimatedFileSize = body.getFileSize();
        if (estimatedFileSize <= 0) {
            throw new IllegalArgumentException("Tamanho do arquivo deve ser informado.");
        }

        UserEntity user = AuthenticatedUserUtils.requireEnrichedUser();

        // Verificar se há espaço suficiente
        checkStorageLimits(user.getId(), estimatedFileSize);

        String objectKey = slugify.slugify(videoName) + "-" + UUID.randomUUID() + "." +
                body.getContentType().substring(6);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(objectKey)
                    .contentType(body.getContentType())
                    .overrideConfiguration(ov -> ov.putRawQueryParameter("x-amz-tagging", "expire=1d"))
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner
                    .presignPutObject(r -> r
                            .signatureDuration(Duration.ofMinutes(PUT_VIDEO_URL_EXPIRATION_IN_MINUTES))
                            .putObjectRequest(putObjectRequest)
                    );

            URL signedUrl = presignedRequest.url();

            UploadedVideoEntity uploadedVideoEntity = new UploadedVideoEntity();
            uploadedVideoEntity.setName(videoName);
            uploadedVideoEntity.setObjectKey(objectKey);
            uploadedVideoEntity.setUser(user);
            uploadedVideoEntity.setSize(estimatedFileSize);

            UploadedVideoEntity saved = uploadedVideoRepository.save(uploadedVideoEntity);

            return SignedUrlDTO.builder()
                    .signedUrl(signedUrl.toString())
                    .videoId(saved.getId())
                    .expirationDate(ZonedDateTime.now().plusMinutes(PUT_VIDEO_URL_EXPIRATION_IN_MINUTES))
                    .build();
        } catch (SdkClientException e) {
            log.error("VideoServiceImpl.requestSignedUrlToPostVideo - error - videoName {}", videoName, e);
            throw e;
        }
    }
	
	private UploadedVideoEntity getUploadedVideoById(Long id) {
		return uploadedVideoRepository.findById(id).orElseThrow(() -> new VideoNotFoundException("Não foi encontrado video de id: " + id));
	}

}
