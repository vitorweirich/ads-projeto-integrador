package com.github.fileshare.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.fileshare.services.SchedulerService;
import com.github.fileshare.services.VideoStorageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveExpiredLinksScheduler {

	private final SchedulerService service;
	private final VideoStorageService videoStorageService;
	
	// TODO: Adicionar shedlock
	@Scheduled(cron = "0 */10 * * * *")
    public void removeExpiredLinks() {
		service.removeExpiredLinks();
    }
	
	@Scheduled(cron = "0 */10 * * * *")
    public void removeNotUploadedVideos() {
		service.removeNotUploadedVideos();
    }
	
	@Scheduled(cron = "0 */10 * * * *")
    public void removeExpiredVideos() {
		videoStorageService.removeExpiredVideos();
    }
	
}
