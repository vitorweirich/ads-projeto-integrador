package com.github.fileshare.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.fileshare.config.JwtProperties;
import com.github.fileshare.config.entities.RefreshTokenEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.response.TokenResponse;
import com.github.fileshare.exceptions.AuthenticationException;
import com.github.fileshare.respositories.RefreshTokenRepository;
import com.github.fileshare.utils.AuthorizationUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepo;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final JwtProperties jwtProperties;

    @Transactional
    public TokenResponse rotateToken(HttpServletRequest request, HttpServletResponse response) {
    	String refreshToken = AuthorizationUtils.extractRefreshTokenFromRequest(request)
    			.orElseThrow(() -> new AuthenticationException("Refresh token is mandatory!"));
    	
        RefreshTokenEntity current = tokenRepo.findById(UUID.fromString(refreshToken))
                .orElseThrow(() -> new AuthenticationException("Token inválido"));

        if (current.isRevoked() || current.getExpiresAt().isBefore(Instant.now())) {
        	tokenRevocationService.revokeFamilyInNewTransaction(current.getFamilyId());
            throw new AuthenticationException("Token inválido");
        }

        RefreshTokenEntity newToken = new RefreshTokenEntity();
        newToken.setUserId(current.getUserId());
        newToken.setFamilyId(current.getFamilyId());
        
        Instant now = Instant.now();
        newToken.setCreatedAt(now);
        newToken.setExpiresAt(now.plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.SECONDS));
        
        newToken.setUserAgent(request.getHeader("User-Agent"));
        newToken.setIpAddress(request.getRemoteAddr());

        current.setRevoked(true);
        current.setReplacedBy(newToken.getId().toString());

        tokenRepo.saveAll(List.of(current, newToken));

        String accessToken = jwtService.generateAccessToken(current.getUserId());

        AuthorizationUtils.injectAccessTokenCookieIntoResponse(accessToken, response);
        AuthorizationUtils.injectRefreshTokenCookieIntoResponse(newToken.getId().toString(), response);

        return TokenResponse.builder()
        	.accessToken(accessToken)
        	.refreshToken(newToken.getId().toString())
        	.build();
    }
    
    public RefreshTokenEntity generateRefreshToken(HttpServletRequest request, UserEntity user) {
		String familyId = UUID.randomUUID().toString();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUserId(user.getId());
        refreshToken.setFamilyId(familyId);
        
        Instant now = Instant.now();
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.SECONDS));
        
        refreshToken.setUserAgent(request.getHeader("User-Agent"));
        refreshToken.setIpAddress(request.getRemoteAddr());

        refreshToken = tokenRepo.save(refreshToken);
		return refreshToken;
	}

}
