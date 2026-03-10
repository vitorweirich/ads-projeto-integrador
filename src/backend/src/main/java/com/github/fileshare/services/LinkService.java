package com.github.fileshare.services;

import java.time.ZonedDateTime;

import com.github.fileshare.dto.response.ShortLinkDTO;

public interface LinkService {

	ShortLinkDTO shortenUrl(String originalUrl, ZonedDateTime expiresIn);

	String getOriginalUrl(String shortUrl);

}
