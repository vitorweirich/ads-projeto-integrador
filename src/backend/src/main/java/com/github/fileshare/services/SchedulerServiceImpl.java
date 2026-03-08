package com.github.fileshare.services;

import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import com.github.fileshare.respositories.UploadedVideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

	private final UploadedVideoRepository uploadedVideoRepository;
	
	@Override
	public void removeExpiredLinks() {
		log.info("SchedulerServiceImpl.removeExpiredLinks - start");
		long start = System.currentTimeMillis();
		
		int deleteByExpired = uploadedVideoRepository.updateExpiredLinks(ZonedDateTime.now());

		log.info("SchedulerServiceImpl.removeExpiredLinks - end - updateCount [{}] - took [{}]ms", deleteByExpired, System.currentTimeMillis() - start);
	}
	
	@Override
	public void removeNotUploadedVideos() {
		log.info("SchedulerServiceImpl.removeNotUploadedVideos - start");
		long start = System.currentTimeMillis();
		
		int deleteByExpired = uploadedVideoRepository
				.deleteByNotUploaded(ZonedDateTime.now().minusMinutes(VideoServiceImpl.PUT_VIDEO_URL_EXPIRATION_IN_MINUTES * 2));

		log.info("SchedulerServiceImpl.removeNotUploadedVideos - end - deleteCount [{}] - took [{}]ms", deleteByExpired, System.currentTimeMillis() - start);
	}

}
