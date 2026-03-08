package com.github.fileshare.services;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.fileshare.config.entities.MagicLinkTokenEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.response.TokenResponse;
import com.github.fileshare.exceptions.AuthenticationException;
import com.github.fileshare.respositories.MagicLinkTokenRepository;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class MagicLinkService {

    @Value("${app.magic-link.base-url}")
    private String baseUrl; // Ex: https://meusite.com/magic-login

    private final MagicLinkTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final AuthService authService;

    public MagicLinkService(MagicLinkTokenRepository tokenRepository, EmailService emailService,
    		UserService userService,
    		AuthService authService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.authService = authService;
    }

    public void generateAndSend(String email) throws MessagingException, IOException {
    	UserEntity loadUserByUsername = (UserEntity) userService.loadUserByUsername(email);
    	
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(10));

        MagicLinkTokenEntity magicToken = new MagicLinkTokenEntity();
        magicToken.setEmail(email);
        magicToken.setToken(token);
        magicToken.setCreatedAt(now);
        magicToken.setExpiresAt(expiresAt);

        tokenRepository.save(magicToken);

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", loadUserByUsername.getName());
        variables.put("magicLink", baseUrl + "/v1/api/auth/login/magic-link?token=" + token);
        emailService.sendHtmlEmailFromTemplate(loadUserByUsername.getEmail(), "Seu link mágico de acesso", "magic-link-email", variables);;
    }
    
    public TokenResponse authenticateUserWithMagicLink(@RequestParam String token,
    		HttpServletResponse response,
            HttpServletRequest request) {
    	MagicLinkTokenEntity magicToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Link inválido"));

        if (magicToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationException("Link expirado");
        }
        
        UserDetails userDetails = userService.loadUserByUsername(magicToken.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null, // sem senha, pois já está autenticado por token
            userDetails.getAuthorities()
        );

        UserEntity user = (UserEntity) authentication.getPrincipal();
        
        // Por hora iremos ignorar o MFA nesse fluxo
//        if(Boolean.TRUE.equals(user.getMfaVerified())) {
//        	// Gera JWT temporário com claim "mfa_pending": true
//        	String tempJwt = jwtUtils.generateJwtTokenWithMfaPending(authentication);
//            return ResponseEntity.ok(MfaJwtResponse.builder().token(tempJwt).build());
//        }
//        
//        if (user.getMfaSecret() != null && !user.getMfaSecret().isEmpty()) {
//            // Gera JWT temporário com claim "mfa_setup": true para finalizar o setup
//            String tempJwt = jwtUtils.generateMfaSetupToken(user.getEmail());
//            return ResponseEntity.ok(MfaJwtResponse.builder().token(tempJwt).type("start_mfa_setup_token").build());
//        }
        
        tokenRepository.delete(magicToken);
        
        TokenResponse tokenResponse = authService.generateAuthenticationTokens(response, request, user);

        return tokenResponse;
    }
}

