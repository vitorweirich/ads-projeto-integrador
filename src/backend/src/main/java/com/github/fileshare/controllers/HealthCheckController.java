package com.github.fileshare.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

	@GetMapping("/health")
    public ResponseEntity<Object> health() {
    	return ResponseEntity.ok().build();
    }
	
}
