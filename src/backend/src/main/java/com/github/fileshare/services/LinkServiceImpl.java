package com.github.fileshare.services;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.github.fileshare.config.entities.UploadedVideoEntity;
import com.github.fileshare.dto.response.ShortLinkDTO;
import com.github.fileshare.exceptions.VideoNotFoundException;
import com.github.fileshare.respositories.UploadedVideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

	private final UploadedVideoRepository uploadedVideoRepository;
	
	@Value("${app.share.base-url:http://localhost:8080/v1/share-url}")
    private String shortUrlBaseUrl;
	
	@Override
	public ShortLinkDTO shortenUrl(String originalUrl, ZonedDateTime expiresIn, UploadedVideoEntity video) {
		String hash = DigestUtils.md5DigestAsHex(originalUrl.getBytes()).substring(0, 8);
		
        String shortUrl = shortUrlBaseUrl + "/" + hash;
        
        ShortLinkDTO shortLink = new ShortLinkDTO();
        shortLink.setId(hash);
        shortLink.setOriginalUrl(originalUrl);
        shortLink.setShortUrl(shortUrl);
        shortLink.setExpiresIn(expiresIn);
        
		return shortLink;
	}

	@Override
	public String getOriginalUrl(String id) {
		UploadedVideoEntity findById = uploadedVideoRepository.findByShortUrlHash(id).orElseThrow(() -> new VideoNotFoundException("Não foi encontrado uma url encurtado com o id: " + id));
		
		return findById.getOriginalUrl();
	}

}
