package com.github.fileshare.utils;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.github.fileshare.dto.response.AdminFileSignedUrl;
import com.github.fileshare.exceptions.MessageFeedbackException;

public class PreSignedUrlUtils {

	public static class UrlTimestamps {
        public final ZonedDateTime creationTime;
        public final ZonedDateTime expirationTime;

        public UrlTimestamps(ZonedDateTime creationTime, ZonedDateTime expirationTime) {
            this.creationTime = creationTime;
            this.expirationTime = expirationTime;
        }

        @Override
        public String toString() {
            return "Creation: " + creationTime + ", Expiration: " + expirationTime;
        }
    }
	
	public static AdminFileSignedUrl extractTimestampsFromPreSignedUrl(AdminFileSignedUrl file) {
		UrlTimestamps extrectedTimestamps = extractTimestampsFromPreSignedUrl(file.getSignedUrl());
		file.setTimestamps(extrectedTimestamps);
		
		return file;
	}

    public static UrlTimestamps extractTimestampsFromPreSignedUrl(String preSignedUrl) {
        try {
            URL url = new URL(preSignedUrl);
            String query = url.getQuery();
            Map<String, String> params = parseQueryParams(query);

            String amzDate = params.get("X-Amz-Date");
            String expires = params.get("X-Amz-Expires");

            if (amzDate == null || expires == null) {
                throw new IllegalArgumentException("URL missing required parameters: X-Amz-Date or X-Amz-Expires");
            }

            ZonedDateTime creationTime = parseAmzDate(amzDate);
            ZonedDateTime expirationTime = creationTime.plusSeconds(Long.parseLong(expires));

            return new UrlTimestamps(creationTime, expirationTime);

        } catch (Exception e) {
            throw new MessageFeedbackException("Failed to parse pre-signed URL", e);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    private static ZonedDateTime parseAmzDate(String amzDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC);
        return ZonedDateTime.parse(amzDate, formatter);
    }
	
}
