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

import com.github.fileshare.config.StorageProperties;
import com.github.fileshare.config.entities.UploadedFileEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.config.entities.UserSettingsEntity;
import com.github.fileshare.dto.internal.SimplifiedUser;
import com.github.fileshare.dto.request.ListFilesRequestParams;
import com.github.fileshare.dto.response.FileDTO;
import com.github.fileshare.dto.response.RequestPostFileDTO;
import com.github.fileshare.dto.response.ShortLinkDTO;
import com.github.fileshare.dto.response.SignedUrlDTO;
import com.github.fileshare.exceptions.FileNotFoundException;
import com.github.fileshare.exceptions.MessageFeedbackException;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.mappers.FileMapper;
import com.github.fileshare.respositories.UploadedFileRepository;
import com.github.fileshare.respositories.UserSettingsRepository;
import com.github.fileshare.utils.AuthenticatedUserUtils;
import com.github.slugify.Slugify;

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
public class FileServiceImpl implements FileService {

	private final S3Presigner presigner;
    private final UploadedFileRepository uploadedFileRepository;
    private final LinkService linkService;
    private final FileMapper fileMapper;
    private final StorageProperties storageProperties;
    private final UserSettingsRepository userSettingsRepository;
    private final S3Client s3Client;
    private final Slugify slugify = Slugify.builder().build();

    public static final String BUCKET_NAME = "files";
    private static final Integer SHARE_URL_EXPIRATION_IN_MINUTES = 120;
    public static final Integer PUT_FILE_URL_EXPIRATION_IN_MINUTES = 60;
    private static final Integer SHARE_URL_TOLERATION_IN_MINUTES = 30;

    // -------------------------
    // Métodos auxiliares
    // -------------------------

    /**
     * Calcula o uso total de armazenamento por um usuário.
     */
    private long getUserStorageUsage(Long userId) {
        return uploadedFileRepository.sumSizeByUserId(userId).orElse(0L);
    }

    /**
     * Calcula o uso total de armazenamento no sistema.
     */
    private long getTotalStorageUsage() {
        return uploadedFileRepository.sumAllSizes().orElse(0L);
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
            throw new MessageFeedbackException("Limite de armazenamento por usuário excedido. Tente excluir alguns arquivos ou aguarde que eles expirem automaticamente!");
        }

        if (currentTotalUsage + newFileSize > storageProperties.getMaxStorageUsageTotal()) {
            throw new MessageFeedbackException("Limite total de armazenamento do sistema excedido. Aguarde algumas horas e tente novamente");
        }
    }
	
	@Override
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
	
	@Override
	@Transactional
    public void deleteFile(Long fileId) {
        UploadedFileEntity file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Não foi encontrado arquivo de id: " + fileId));
		
        SimplifiedUser simplifiedUser = AuthenticatedUserUtils.requireSimplifiedUser();
        if(!simplifiedUser.getEmail().equals(file.getUser().getEmail())) {
            throw new AccessDeniedException("Você não tem permissão para remover esse arquivo!");
        }
		
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket(FileServiceImpl.BUCKET_NAME)
            .key(file.getObjectKey())
            .build();
        
        try {
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            log.warn("FileServiceImpl.deleteFile - error deleting file from storage - fileId {}", fileId, e);
        }
        
        uploadedFileRepository.delete(file);
	}
	
    private Specification<UploadedFileEntity> createListFileSpecification(ListFilesRequestParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            SimplifiedUser simplifiedUser = AuthenticatedUserUtils.requireSimplifiedUser();
            
            predicates.add(cb.equal(root.get("user").get("email"), simplifiedUser.getEmail()));
            
            return cb.and(predicates.toArray(new Predicate[]{}));
        };
    }
	
	@Override
	@Transactional
    public SignedUrlDTO requestSignedUrlToGetFile(Long fileId) {
        try {
            UploadedFileEntity uploadedFileById = this.getUploadedFileById(fileId);
        	
        	SimplifiedUser user = AuthenticatedUserUtils.requireSimplifiedUser();
            if(!user.getEmail().equals(uploadedFileById.getUser().getEmail())) {
                throw new AccessDeniedException("Você não tem permissão para requisitar esse arquivo!");
            }
        	
            if(!uploadedFileById.isUploaded()) {
                throw new MessageFeedbackException("O upload do arquivo com id [%s] não foi finalizado!".formatted(fileId));
        	}
        	
        	ZonedDateTime now = ZonedDateTime.now();
        	
            Map<String, Object> metadata = Map.of(
                    "fileName", uploadedFileById.getName(),
                    "creatorName", user.getName(),
                    "creatorEmail", user.getEmail(),
                    "fileType", uploadedFileById.getContentType()
                );
        	
            if(StringUtils.hasText(uploadedFileById.getShareUrl()) && uploadedFileById.getExpiresIn()
                .isAfter(now.plusMinutes(SHARE_URL_TOLERATION_IN_MINUTES))) {
            
                return SignedUrlDTO.builder()
                    .signedUrl(uploadedFileById.getShareUrl())
                    .metadata(metadata)
                    .fileId(fileId)
                    .expirationDate(uploadedFileById.getExpiresIn())
                    .build();
        	}
        	
            GetObjectRequest putObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(uploadedFileById.getObjectKey())
                .build();
        	
        	
            // TODO: Calcular quanto tempo o arquivo ainda estará disponível com base na data de criação
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(r -> r.signatureDuration(Duration.ofMinutes(SHARE_URL_EXPIRATION_IN_MINUTES))
                .getObjectRequest(putObjectRequest));

            URL signedUri = presignedRequest.url();
        
            String signedUrl = signedUri.toString();
        
            ZonedDateTime expiresIn = now.plusMinutes(SHARE_URL_EXPIRATION_IN_MINUTES);
        
            ShortLinkDTO shortenUrl = linkService.shortenUrl(signedUrl, expiresIn);
        
            uploadedFileById.setShareUrl(shortenUrl.getShortUrl());
            uploadedFileById.setExpiresIn(expiresIn);
            uploadedFileById.setOriginalUrl(shortenUrl.getOriginalUrl());
            uploadedFileById.setShortUrlHash(shortenUrl.getId());
        
            uploadedFileRepository.save(uploadedFileById);
        
            return SignedUrlDTO.builder()
                .signedUrl(shortenUrl.getShortUrl())
                .metadata(metadata)
                .fileId(fileId)
                .expirationDate(uploadedFileById.getExpiresIn())
                .build();
        } catch (SdkClientException e) {
            log.error("FileServiceImpl.requestSignedUrlToGetFile - error - fileId {}", fileId, e);
            throw e;
        }
	}
	
	@Override
    public SignedUrlDTO requestSignedUrlToPostFile(RequestPostFileDTO body) {
        String fileName = body.getFileName();

        // Aqui assumimos que o tamanho do arquivo foi informado no DTO (bytes)
        long estimatedFileSize = body.getFileSize();
        if (estimatedFileSize <= 0) {
            throw new IllegalArgumentException("Tamanho do arquivo deve ser informado.");
        }

        UserEntity user = AuthenticatedUserUtils.requireEnrichedUser();

        // Verificar se há espaço suficiente
        checkStorageLimits(user.getId(), estimatedFileSize);

        String objectKey = slugify.slugify(fileName) + "-" + UUID.randomUUID() + "." +
            body.getContentType().split("/")[1];

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .contentType(body.getContentType())
                .overrideConfiguration(ov -> ov.putRawQueryParameter("x-amz-tagging", "expire=1d"))
                .build();

            PresignedPutObjectRequest presignedRequest = presigner
                .presignPutObject(r -> r
                    .signatureDuration(Duration.ofMinutes(PUT_FILE_URL_EXPIRATION_IN_MINUTES))
                    .putObjectRequest(putObjectRequest)
                );

            URL signedUrl = presignedRequest.url();

            UploadedFileEntity uploadedFileEntity = new UploadedFileEntity();
            uploadedFileEntity.setName(fileName);
            uploadedFileEntity.setObjectKey(objectKey);
            uploadedFileEntity.setUser(user);
            uploadedFileEntity.setSize(estimatedFileSize);
            uploadedFileEntity.setContentType(body.getContentType());

            UploadedFileEntity saved = uploadedFileRepository.save(uploadedFileEntity);

            return SignedUrlDTO.builder()
            .signedUrl(signedUrl.toString())
            .fileId(saved.getId())
            .expirationDate(ZonedDateTime.now().plusMinutes(PUT_FILE_URL_EXPIRATION_IN_MINUTES))
            .metadata(Map.of("contentType", body.getContentType()))
            .build();
        } catch (SdkClientException e) {
            log.error("FileServiceImpl.requestSignedUrlToPostFile - error - fileName {}", fileName, e);
            throw e;
        }
        }
	
	private UploadedFileEntity getUploadedFileById(Long id) {
		return uploadedFileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("Não foi encontrado arquivo de id: " + id));
	}

}
