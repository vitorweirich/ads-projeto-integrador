package com.github.fileshare.schedulers;

import java.time.ZonedDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.fileshare.respositories.TemporaryUserRepository;

@Component
public class UsuarioTemporarioCleanupScheduler {
    private final TemporaryUserRepository usuarioTemporarioRepository;

    public UsuarioTemporarioCleanupScheduler(TemporaryUserRepository usuarioTemporarioRepository) {
        this.usuarioTemporarioRepository = usuarioTemporarioRepository;
    }

    @Scheduled(fixedRate = 3600000) // roda a cada 1 hora
    public void cleanupExpiredTemporaryUsers() {
        usuarioTemporarioRepository.deleteByExpiresInDateBefore(ZonedDateTime.now().minusMinutes(5));
    }
}
