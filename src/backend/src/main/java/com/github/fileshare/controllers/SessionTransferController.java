package com.github.fileshare.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.fileshare.dto.request.SessionExchangeRequest;
import com.github.fileshare.dto.request.SessionTransferRequest;
import com.github.fileshare.dto.response.SessionTransferResponse;
import com.github.fileshare.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class SessionTransferController {

	private final AuthService authService;
	
	/**
     * Gera um token temporário para transferir a sessão
     * Requer usuário autenticado
     */
    @PostMapping("/session-transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionTransferResponse> generateSessionTransferToken(@Valid @RequestBody SessionTransferRequest request) {
    	SessionTransferResponse response = authService.createSessionTransferToken(request.getTarget());

        return ResponseEntity.ok(response);
    }

    /**
     * Troca o token temporário por um JWT válido
     */
    @PostMapping("/session-exchange")
    public ResponseEntity<?> exchangeSession(@RequestBody SessionExchangeRequest body,
    		HttpServletResponse response,
            HttpServletRequest request) {
    	
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return authService.exchangeSession(body.getToken(), response, request);
    }
	
}
