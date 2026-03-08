package com.github.fileshare.services;

public interface SchedulerService {

	public void removeExpiredLinks();
	
	public void removeNotUploadedVideos();
	
}
