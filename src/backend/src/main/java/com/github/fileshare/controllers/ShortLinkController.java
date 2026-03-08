package com.github.fileshare.controllers;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.fileshare.services.LinkService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/v1/share-url")
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final LinkService linkService;
    
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Object> redirect(@PathVariable String shortUrl, @RequestParam(defaultValue = "false") boolean jsonResponse) {
    	String originalUrl = linkService.getOriginalUrl(shortUrl);
    	URI uri = URI.create(originalUrl);
    	
    	if(jsonResponse) {
    		return ResponseEntity.ok(Map.of("url", uri));
    	}
    	
    	HttpHeaders httpHeaders = new HttpHeaders();
    	httpHeaders.setLocation(uri);
    	return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }
}
