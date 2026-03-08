package com.github.fileshare.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.fileshare.dto.request.ListVideosRequestParams;
import com.github.fileshare.dto.response.RequestPostVideoDTO;
import com.github.fileshare.dto.response.SignedUrlDTO;
import com.github.fileshare.dto.response.VideoDTO;
import com.github.fileshare.services.VideoService;
import com.github.fileshare.services.VideoStorageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/v1/videos")
@RestController
@RequiredArgsConstructor
public class VideoController {
	
	private final VideoService service;
	private final VideoStorageService videoStorageService;
	
	@GetMapping("/me")
	public ResponseEntity<Page<VideoDTO>> myVideos(@Valid @ModelAttribute ListVideosRequestParams params) {
		return ResponseEntity.ok(service.listVideos(params));
	}
	
	@GetMapping("/{videoId}")
	public ResponseEntity<SignedUrlDTO> requestSignedUrlToGetVideo(@PathVariable Long videoId) {
		return ResponseEntity.ok(service.requestSignedUrlToGetVideo(videoId));
	}
	
	@PostMapping("/upload")
	public ResponseEntity<SignedUrlDTO> requestSignedUrlToPostVideo(@Valid @RequestBody RequestPostVideoDTO body) {
		return ResponseEntity.ok(service.requestSignedUrlToPostVideo(body));
	}
	
	@PatchMapping("/upload/{videoId}/register-uploaded")
	public ResponseEntity<Void> setVideoUploaded(@PathVariable Long videoId) {
		videoStorageService.setVideoUploaded(videoId);
		return ResponseEntity.noContent().header("X-Client-Action", "refresh-user").build();
	}
	
	@DeleteMapping("/{videoId}")
	public ResponseEntity<SignedUrlDTO> deleteVideo(@PathVariable Long videoId) {
		service.deleteVideo(videoId);
		
		return ResponseEntity.noContent().header("X-Client-Action", "refresh-user").build();
	}

}
