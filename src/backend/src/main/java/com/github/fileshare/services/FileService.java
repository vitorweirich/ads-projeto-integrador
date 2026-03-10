package com.github.fileshare.services;

import org.springframework.data.domain.Page;

import com.github.fileshare.dto.request.ListFilesRequestParams;
import com.github.fileshare.dto.response.FileDTO;
import com.github.fileshare.dto.response.RequestPostFileDTO;
import com.github.fileshare.dto.response.SignedUrlDTO;

public interface FileService {
	
	public SignedUrlDTO requestSignedUrlToGetFile(Long fileName);

	public SignedUrlDTO requestSignedUrlToPostFile(RequestPostFileDTO body);

	public Page<FileDTO> listFiles(ListFilesRequestParams params);

	void deleteFile(Long fileId);
}
