package com.github.fileshare.schedulers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.fileshare.respositories.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveRevokedRefreshTokensScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredRefreshTokens() {
    	refreshTokenRepository.deleteByExpiresAtDateBefore(Instant.now().minus(5, ChronoUnit.MINUTES));
    }
}
