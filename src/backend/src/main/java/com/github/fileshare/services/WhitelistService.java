package com.github.fileshare.services;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.fileshare.config.entities.EmailWhitelistEntity;
import com.github.fileshare.config.entities.EmailWhitelistEntity.Status;
import com.github.fileshare.dto.request.InviteRequest;
import com.github.fileshare.dto.response.WhitelistEntryDTO;
import com.github.fileshare.exceptions.AuthenticationException;
import com.github.fileshare.exceptions.MessageFeedbackException;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.respositories.EmailWhitelistRepository;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WhitelistService {

    private final EmailWhitelistRepository whitelistRepository;
    private final EmailService emailService;

    @Value("${app.invite.base-url:http://localhost:5173/register}")
    private String inviteBaseUrl;

    @Value("${app.invite.expiration-hours:72}")
    private long inviteExpirationHours;

    public Status checkEmailStatus(String email) {
        return whitelistRepository.findByEmail(email)
                .map(EmailWhitelistEntity::getStatus)
                .orElse(null); // null = não está na whitelist
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    public void registerPendingEmail(String email, String name) {
        EmailWhitelistEntity entry = whitelistRepository.findByEmail(email)
                .orElseGet(EmailWhitelistEntity::new);

        if (entry.getStatus() == Status.ALLOWED) return; // já permitido, não faz nada

        entry.setEmail(email);
        entry.setInvitedName(name);
        entry.setStatus(Status.PENDING);
        whitelistRepository.save(entry);
    }

    public Page<WhitelistEntryDTO> listPending(int page, int rows) {
        PageRequest pageable = PageRequest.of(page, rows, Sort.by(Sort.Direction.DESC, "createdAt"));
        return whitelistRepository.findAllByStatus(Status.PENDING, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public void approveAndInvite(Long id) {
        EmailWhitelistEntity entry = whitelistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrada não encontrada"));

        sendInvite(entry);
    }

    @Transactional
    public void invite(InviteRequest request) {
        EmailWhitelistEntity entry = whitelistRepository.findByEmail(request.getEmail())
                .orElseGet(EmailWhitelistEntity::new);

        entry.setEmail(request.getEmail());
        entry.setInvitedName(request.getName());
        entry.setStatus(Status.ALLOWED);
        whitelistRepository.save(entry);

        sendInvite(entry);
    }

    @Transactional
    public void consumeInviteToken(String token) {
        EmailWhitelistEntity entry = whitelistRepository.findByInviteToken(token)
                .orElseThrow(() -> new AuthenticationException("Token de convite inválido"));

        if (entry.getInviteTokenExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthenticationException("Token de convite expirado");
        }

        whitelistRepository.save(entry);
    }

    private void sendInvite(EmailWhitelistEntity entry) {
        String token = UUID.randomUUID().toString();
        entry.setStatus(Status.ALLOWED);
        entry.setInviteToken(token);
        entry.setInviteTokenExpiresAt(ZonedDateTime.now().plusHours(inviteExpirationHours));
        whitelistRepository.save(entry);

        String inviteUrl = inviteBaseUrl + "?invite=" + token;
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", StringUtils.hasLength(entry.getInvitedName()) ? entry.getInvitedName() : entry.getEmail());
        variables.put("inviteUrl", inviteUrl);
        variables.put("expirationHours", inviteExpirationHours);

        try {
            emailService.sendHtmlEmailFromTemplate(entry.getEmail(), "Você foi convidado!", "invite-email", variables);
        } catch (MessagingException | IOException e) {
            throw new MessageFeedbackException("Falha ao enviar email de convite");
        }
    }

    private WhitelistEntryDTO toDTO(EmailWhitelistEntity e) {
        WhitelistEntryDTO dto = new WhitelistEntryDTO();
        dto.setId(e.getId());
        dto.setEmail(e.getEmail());
        dto.setInvitedName(e.getInvitedName());
        dto.setStatus(e.getStatus());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    public WhitelistEntryDTO getInviteInfo(String token) {
        EmailWhitelistEntity entry = whitelistRepository.findByInviteToken(token)
                .orElseThrow(() -> new AuthenticationException("Token de convite inválido"));
        if (entry.getInviteTokenExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthenticationException("Token de convite expirado");
        }
        return toDTO(entry);
    }
}
