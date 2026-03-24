package com.github.fileshare.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.fileshare.services.SchedulerService;
import com.github.fileshare.services.FileStorageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveExpiredLinksScheduler {

	private final SchedulerService service;
	private final FileStorageService fileStorageService;
	
	// TODO: Adicionar shedlock (para rodar em ambiente clusterizado)
	@Scheduled(cron = "0 */10 * * * *")
    public void removeExpiredLinks() {
		service.removeExpiredLinks();
    }
	
	@Scheduled(cron = "0 */10 * * * *")
    public void removeNotUploadedFiles() {
		service.removeNotUploadedFiles();
    }
	
	@Scheduled(cron = "0 */10 * * * *")
    public void removeExpiredFiles() {
		fileStorageService.removeExpiredFiles();
    }
	
}
