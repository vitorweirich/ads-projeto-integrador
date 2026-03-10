package com.github.fileshare.services;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.fileshare.config.entities.UploadedFileEntity;
import com.github.fileshare.exceptions.FileNotFoundException;
import com.github.fileshare.respositories.UploadedFileRepository;

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
public class FileStorageService {

    private final S3Client s3Client;
    private final UploadedFileRepository uploadedFileRepository;

    public void setFileUploaded(Long id) {
    	UploadedFileEntity uploadedFileById = this.getUploadedFileById(id);
		
		uploadedFileById.setUploaded(true);
		
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(FileServiceImpl.BUCKET_NAME)
                .key(uploadedFileById.getObjectKey())
                .build();

        HeadObjectResponse headResponse = s3Client.headObject(headRequest);
        long realSize = headResponse.contentLength();

        uploadedFileById.setSize(realSize);
        
        uploadedFileRepository.save(uploadedFileById);
    }
    
	public void removeExpiredFiles() {
		log.info("SchedulerServiceImpl.removeExpiredFiles - start");
		long start = System.currentTimeMillis();
		
		// TODO: Para isso escalar, precisa processar assincronamente, se tiver muitos arquivos, isso nao pode ser sincrono
		// Deve ser limitado a 1000 itens por página
		List<UploadedFileEntity> expiredFiles = uploadedFileRepository.findByCreatedAtBefore(ZonedDateTime.now().minusDays(1));
		
		if(expiredFiles.size() == 0) {
			log.info("SchedulerServiceImpl.removeExpiredFiles - end - no files to delete - took [{}]ms", System.currentTimeMillis() - start);
			return;
		}
		
		this.deleteFilesFromStorage(expiredFiles);
		
		uploadedFileRepository.deleteAll(expiredFiles);

		log.info("SchedulerServiceImpl.removeExpiredFiles - end - deleteCount [{}] - took [{}]ms", expiredFiles.size(), System.currentTimeMillis() - start);
	}
    
    public void deleteFilesFromStorage(List<UploadedFileEntity> files) {
	    List<ObjectIdentifier> toDelete = files.stream()
	        .map(v -> ObjectIdentifier.builder().key(v.getObjectKey()).build())
	        .toList();

	    DeleteObjectsRequest request = DeleteObjectsRequest.builder()
	        .bucket(FileServiceImpl.BUCKET_NAME)
	        .delete(Delete.builder().objects(toDelete).build())
	        .build();
	    
	    s3Client.deleteObjects(request);
	}
    
    private UploadedFileEntity getUploadedFileById(Long id) {
		return uploadedFileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("Não foi encontrado arquivo de id: " + id));
	}
}

