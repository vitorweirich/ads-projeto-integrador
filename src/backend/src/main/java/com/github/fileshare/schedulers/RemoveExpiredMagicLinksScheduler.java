package com.github.fileshare.schedulers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.fileshare.respositories.MagicLinkTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveExpiredMagicLinksScheduler {
    private final MagicLinkTokenRepository magicLinkTokenRepository;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredUsuariosTemporarios() {
    	magicLinkTokenRepository.deleteByExpiresAtDateBefore(Instant.now().minus(5, ChronoUnit.MINUTES));
    }
}
