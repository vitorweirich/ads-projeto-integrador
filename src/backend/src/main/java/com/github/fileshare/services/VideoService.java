package com.github.fileshare.services;

import org.springframework.data.domain.Page;

import com.github.fileshare.dto.request.ListVideosRequestParams;
import com.github.fileshare.dto.response.RequestPostVideoDTO;
import com.github.fileshare.dto.response.SignedUrlDTO;
import com.github.fileshare.dto.response.VideoDTO;

public interface VideoService {
	
	public SignedUrlDTO requestSignedUrlToGetVideo(Long videoName);

	public SignedUrlDTO requestSignedUrlToPostVideo(RequestPostVideoDTO body);

	public Page<VideoDTO> listVideos(ListVideosRequestParams params);

	void deleteVideo(Long videoId);
}
