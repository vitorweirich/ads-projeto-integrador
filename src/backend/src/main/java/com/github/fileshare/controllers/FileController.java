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

import com.github.fileshare.dto.request.ListFilesRequestParams;
import com.github.fileshare.dto.response.FileDTO;
import com.github.fileshare.dto.response.RequestPostFileDTO;
import com.github.fileshare.dto.response.SignedUrlDTO;
import com.github.fileshare.services.FileService;
import com.github.fileshare.services.FileStorageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/v1/videos")
@RestController
@RequiredArgsConstructor
public class FileController {
	
	private final FileService service;
	private final FileStorageService fileStorageService;
	
	@GetMapping("/me")
	public ResponseEntity<Page<FileDTO>> listFiles(@Valid @ModelAttribute ListFilesRequestParams params) {
		return ResponseEntity.ok(service.listFiles(params));
	}
	
	@GetMapping("/{fileId}")
	public ResponseEntity<SignedUrlDTO> requestSignedUrlToGetFile(@PathVariable Long fileId) {
		return ResponseEntity.ok(service.requestSignedUrlToGetFile(fileId));
	}
	
	@PostMapping("/upload")
	public ResponseEntity<SignedUrlDTO> requestSignedUrlToPostFile(@Valid @RequestBody RequestPostFileDTO body) {
		return ResponseEntity.ok(service.requestSignedUrlToPostFile(body));
	}
	
	@PatchMapping("/upload/{fileId}/register-uploaded")
	public ResponseEntity<Void> setFileUploaded(@PathVariable Long fileId) {
		fileStorageService.setFileUploaded(fileId);
		return ResponseEntity.noContent().header("X-Client-Action", "refresh-user").build();
	}
	
	@DeleteMapping("/{fileId}")
	public ResponseEntity<SignedUrlDTO> deleteFile(@PathVariable Long fileId) {
		service.deleteFile(fileId);
		
		return ResponseEntity.noContent().header("X-Client-Action", "refresh-user").build();
	}

}
