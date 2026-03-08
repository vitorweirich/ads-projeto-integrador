package com.github.fileshare.services;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.fileshare.config.entities.UploadedVideoEntity;
import com.github.fileshare.exceptions.VideoNotFoundException;
import com.github.fileshare.respositories.UploadedVideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoStorageService {

    private final S3Client s3Client;
    private final UploadedVideoRepository uploadedVideoRepository;

    public void setVideoUploaded(Long id) {
    	UploadedVideoEntity uploadedVideoById = this.getUploadedVideoById(id);
		
		uploadedVideoById.setUploaded(true);
		
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(VideoServiceImpl.BUCKET_NAME)
                .key(uploadedVideoById.getObjectKey())
                .build();

        HeadObjectResponse headResponse = s3Client.headObject(headRequest);
        long realSize = headResponse.contentLength();

        uploadedVideoById.setSize(realSize);
        
        uploadedVideoRepository.save(uploadedVideoById);
    }
    
	public void removeExpiredVideos() {
		log.info("SchedulerServiceImpl.removeExpiredVideos - start");
		long start = System.currentTimeMillis();
		
		// TODO: Para isso escalar, precisa processar assincronamente, se tiver muitos videos, isso nao pode ser sincrono
		// Deve ser limitado a 1000 itens por página
		List<UploadedVideoEntity> expiredVideos = uploadedVideoRepository.findByCreatedAtBefore(ZonedDateTime.now().minusDays(1));
		
		if(expiredVideos.size() == 0) {
			log.info("SchedulerServiceImpl.removeExpiredVideos - end - no videos to delete - took [{}]ms", System.currentTimeMillis() - start);
			return;
		}
		
		this.deleteVideosFromStorage(expiredVideos);
		
		uploadedVideoRepository.deleteAll(expiredVideos);

		log.info("SchedulerServiceImpl.removeExpiredVideos - end - deleteCount [{}] - took [{}]ms", expiredVideos.size(), System.currentTimeMillis() - start);
	}
    
    public void deleteVideosFromStorage(List<UploadedVideoEntity> videos) {
	    List<ObjectIdentifier> toDelete = videos.stream()
	        .map(v -> ObjectIdentifier.builder().key(v.getObjectKey()).build())
	        .toList();

	    DeleteObjectsRequest request = DeleteObjectsRequest.builder()
	        .bucket(VideoServiceImpl.BUCKET_NAME)
	        .delete(Delete.builder().objects(toDelete).build())
	        .build();
	    
	    s3Client.deleteObjects(request);
	}
    
    private UploadedVideoEntity getUploadedVideoById(Long id) {
		return uploadedVideoRepository.findById(id).orElseThrow(() -> new VideoNotFoundException("Não foi encontrado video de id: " + id));
	}
}

