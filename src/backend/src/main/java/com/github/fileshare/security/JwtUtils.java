package com.github.fileshare.security;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.github.fileshare.config.JwtProperties;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.constants.JwtClaims;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private SecretKey jwtSecret;

    private int jwtExpirationSeconds;
    
    public JwtUtils(JwtProperties jwtProperties) {
        super();
        this.jwtSecret = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.jwtExpirationSeconds = jwtProperties.getAccessTokenExpiration();
    }

    public String generateJwtToken(Authentication authentication) {
        return this.generateJwtToken((UserEntity) authentication.getPrincipal());
    }
    
    public String generateJwtToken(UserEntity userPrincipal) {
    	List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
    	
    	return Jwts.builder()
    			.subject(userPrincipal.getUsername())
    			.claim(JwtClaims.ROLES, roles)
    			.claim(JwtClaims.USER_NAME, userPrincipal.getName())
    			.issuedAt(new Date())
    			.expiration(new Date(System.currentTimeMillis() + (jwtExpirationSeconds * 1000)))
    			.signWith(jwtSecret, Jwts.SIG.HS512)
    			.compact();
    }

    public Claims getClaimsFromJwtToken(String token) {
    	return Jwts.parser()
    			.verifyWith(jwtSecret)
    			.build()
    			.parseSignedClaims(token)
    			.getPayload();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }
    
    public boolean isMfaPending(String token) {
        try {
            var claims = Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
            Object mfaPending = claims.get("mfa_pending");
            return mfaPending != null && Boolean.TRUE.equals(mfaPending);
        } catch (Exception e) {
            return false;
        }
    }
    
    public String generateMfaSetupToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim(JwtClaims.MFA_SETUP, true)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 minutos
                .signWith(jwtSecret, Jwts.SIG.HS512)
                .compact();
    }

    public String generateJwtTokenWithMfaPending(Authentication authentication) {
    	UserEntity userPrincipal = (UserEntity) authentication.getPrincipal();

        // Token temporário, expira rápido (ex: 5 minutos)
        int tempExpirationMs = 5 * 60 * 1000;

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tempExpirationMs))
                .claim(JwtClaims.MFA_PENDING, true)
                .signWith(jwtSecret, Jwts.SIG.HS512)
                .compact();
    }
}
