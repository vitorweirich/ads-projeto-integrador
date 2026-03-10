package com.github.fileshare.services;

import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import com.github.fileshare.respositories.UploadedFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

	private final UploadedFileRepository uploadedFileRepository;
	
	@Override
	public void removeExpiredLinks() {
		log.info("SchedulerServiceImpl.removeExpiredLinks - start");
		long start = System.currentTimeMillis();
		
		int deleteByExpired = uploadedFileRepository.updateExpiredLinks(ZonedDateTime.now());

		log.info("SchedulerServiceImpl.removeExpiredLinks - end - updateCount [{}] - took [{}]ms", deleteByExpired, System.currentTimeMillis() - start);
	}
	
	@Override
	public void removeNotUploadedFiles() {
		log.info("SchedulerServiceImpl.removeNotUploadedFiles - start");
		long start = System.currentTimeMillis();
		
		int deleteByExpired = uploadedFileRepository
				.deleteByNotUploaded(ZonedDateTime.now().minusMinutes(FileServiceImpl.PUT_FILE_URL_EXPIRATION_IN_MINUTES * 2));

		log.info("SchedulerServiceImpl.removeNotUploadedFiles - end - deleteCount [{}] - took [{}]ms", deleteByExpired, System.currentTimeMillis() - start);
	}

}
