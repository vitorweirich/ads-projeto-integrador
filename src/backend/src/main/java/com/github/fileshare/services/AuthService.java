package com.github.fileshare.services;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.github.fileshare.config.StorageProperties;
import com.github.fileshare.config.entities.PasswordResetTokenEntity;
import com.github.fileshare.config.entities.RefreshTokenEntity;
import com.github.fileshare.config.entities.SessionTransferTokenEntity;
import com.github.fileshare.config.entities.TemporaryUserEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.config.entities.UserSettingsEntity;
import com.github.fileshare.dto.request.ForgotPasswordRequest;
import com.github.fileshare.dto.request.LoginRequest;
import com.github.fileshare.dto.request.ResetPasswordRequest;
import com.github.fileshare.dto.request.SignupRequest;
import com.github.fileshare.dto.response.MfaJwtResponse;
import com.github.fileshare.dto.response.SessionTransferResponse;
import com.github.fileshare.dto.response.TokenResponse;
import com.github.fileshare.exceptions.AuthenticationException;
import com.github.fileshare.exceptions.MessageFeedbackException;
import com.github.fileshare.respositories.PasswordResetTokenRepository;
import com.github.fileshare.respositories.SessionTransferTokenRepository;
import com.github.fileshare.respositories.TemporaryUserRepository;
import com.github.fileshare.respositories.UserSettingsRepository;
import com.github.fileshare.security.JwtUtils;
import com.github.fileshare.utils.AuthenticatedUserUtils;
import com.github.fileshare.utils.AuthorizationUtils;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final TemporaryUserRepository temporaryUserRepository;
	private final UserSettingsRepository userSettingsRepository;
	private final StorageProperties storageProperties;
	private final UserService userService;
	private final PasswordEncoder encoder;
	private final EmailService emailService;
	private final TokenRevocationService tokenRevocationService;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final RefreshTokenService refreshTokenService;
	private final SessionTransferTokenRepository sessionTransferTokenRepository;
	
	private static final Duration RESET_PASSWORD_TOKEN_VALIDITY = Duration.ofMinutes(30);
	
	@Value("${app.confirmation.base-url:http://localhost:8080/v1/api/auth/confirm}")
    private String confirmationBaseUrl;
    @Value("${app.confirmation.expiration-minutes:60}")
    private long expirationMinutes;
    @Value("${app.reset-password.base-url:http://localhost:8080/v1/api/auth/reset-password}")
    private String resetPasswordBaseUrl;
    
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest,
            HttpServletResponse response,
            HttpServletRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
		
		UserEntity user = (UserEntity) authentication.getPrincipal();
		
		ResponseEntity<MfaJwtResponse> mfaResponse = this.buildMfaResponseIfExists(authentication, user);
		if(Objects.nonNull(mfaResponse)) {
			return mfaResponse;
		}
		
		TokenResponse tokens = this.generateAuthenticationTokens(response, request, user);
		
		return AuthorizationUtils.buildTokenResponse(request, tokens);
	}

	public TokenResponse generateAuthenticationTokens(HttpServletResponse response, HttpServletRequest request, UserEntity user) {
		String accessToken = jwtUtils.generateJwtToken(user);
		
        RefreshTokenEntity refreshToken = refreshTokenService.generateRefreshToken(request, user);
        
        AuthorizationUtils.injectAccessTokenCookieIntoResponse(accessToken, response);
        AuthorizationUtils.injectRefreshTokenCookieIntoResponse(refreshToken.getId().toString(), response);
        
        return TokenResponse.builder()
        	.accessToken(accessToken)
        	.refreshToken(refreshToken.getId().toString())
        	.build();
	}
	
	public ResponseEntity<MfaJwtResponse> buildMfaResponseIfExists(Authentication authentication, UserEntity user) {
		if (Boolean.TRUE.equals(user.getMfaVerified())) {
            String tempJwt = jwtUtils.generateJwtTokenWithMfaPending(authentication);
            return ResponseEntity.ok(MfaJwtResponse.builder().token(tempJwt).build());
        }
        
        return null;
	}
	
	public ResponseEntity<Void> processUserLogout(HttpServletRequest request, HttpServletResponse response) {
    	AuthorizationUtils.extractRefreshTokenFromRequest(request)
    		.ifPresent(tokenRevocationService::invalidateToken);
    	
    	// TODO: Receber access_token e incluir ele em uma black_list

        AuthorizationUtils.injectCookieWithDefaultsIntoResponse(AuthorizationUtils.ACCESS_TOKEN, "", 0, response);
        AuthorizationUtils.injectCookieWithDefaultsIntoResponse(AuthorizationUtils.REFRESH_TOKEN, "", 0, response);

        return ResponseEntity.noContent().build();
    }
	
    @Transactional
	public void registerTemporaryUser(SignupRequest signUpRequest) {
        var usuarioTempOpt = temporaryUserRepository.findByEmail(signUpRequest.getEmail());
        boolean emailDefinitivo = userService.existsByEmail(signUpRequest.getEmail());
        boolean emailTemporario = usuarioTempOpt.isPresent();
        boolean expirado = false;
        
        ZonedDateTime now = ZonedDateTime.now();
        
        TemporaryUserEntity usuarioTemp = null;
        if (emailTemporario) {
            usuarioTemp = usuarioTempOpt.get();
            expirado = now.isAfter(usuarioTemp.getExpiresAt());
        }
        if (emailDefinitivo || (emailTemporario && !expirado)) {
            throw new AuthenticationException("Este email já está em uso");
        }
        String token = UUID.randomUUID().toString();
        if (!emailTemporario) {
            usuarioTemp = new TemporaryUserEntity();
        }
        
        usuarioTemp.setName(signUpRequest.getName());
        usuarioTemp.setEmail(signUpRequest.getEmail());
        usuarioTemp.setPassword(encoder.encode(signUpRequest.getPassword()));
        usuarioTemp.setConfirmationToken(token);
        usuarioTemp.setExpiresAt(now.plusMinutes(expirationMinutes));
        
        temporaryUserRepository.save(usuarioTemp);
        
        String confirmationUrl = confirmationBaseUrl + "/" + token;
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", signUpRequest.getName());
        variables.put("confirmationUrl", confirmationUrl);
        try {
            emailService.sendHtmlEmailFromTemplate(signUpRequest.getEmail(), "Confirme seu cadastro", "magic-link-confirmation", variables);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            throw new MessageFeedbackException("Falha ao enviar email de confirmação");
        }
    }
	
    @Transactional
	public void confirmEmailAndSavePermanentUser(@PathVariable String token) {
        var usuarioTempOpt = temporaryUserRepository.findByConfirmationToken(token);
        if (usuarioTempOpt.isEmpty()) {
            throw new AuthenticationException("Token de confirmação inválido ou expirado");
        }
        TemporaryUserEntity usuarioTemp = usuarioTempOpt.get();
        if (isTemporaryUserExpired(usuarioTemp)) {
        	temporaryUserRepository.delete(usuarioTemp);
            throw new AuthenticationException("Token expirado. Por favor, faça o cadastro novamente");
        }
        if (userService.existsByEmail(usuarioTemp.getEmail())) {
        	temporaryUserRepository.delete(usuarioTemp);
            throw new AuthenticationException("Este usuário já foi confirmado");
        }
        
        UserEntity user = new UserEntity();
        user.setName(usuarioTemp.getName());
        user.setEmail(usuarioTemp.getEmail());
        user.setPassword(usuarioTemp.getPassword());
        user.setRole("USER");
        user = userService.save(user);
        
        UserSettingsEntity userSettings = new UserSettingsEntity();
        userSettings.setUser(user);
        userSettings.setStorageLimitBytes(storageProperties.getDefaultMaxStorageUsagePerUser());
        userSettings.setMaxFileRetentionDays(storageProperties.getDefaultMaxFileRetentionDays());
        userSettingsRepository.save(userSettings);
        
        temporaryUserRepository.delete(usuarioTemp);
    }
	
	private boolean isTemporaryUserExpired(TemporaryUserEntity usuarioTemp) {
        return ZonedDateTime.now().isAfter(usuarioTemp.getExpiresAt());
    }
	
	@Transactional
	public void generateTokenAndSendEmail(ForgotPasswordRequest request) throws MessagingException, IOException {
        UserEntity user = userService.findByEmail(request.getEmail());
        
        Instant now = Instant.now();

        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setId(UUID.randomUUID());
        token.setUser(user);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(RESET_PASSWORD_TOKEN_VALIDITY));
        passwordResetTokenRepository.save(token);

        String link = resetPasswordBaseUrl + "/" + token.getId();
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getName());
        variables.put("resetLink", link);
        emailService.sendHtmlEmailFromTemplate(user.getEmail(), "Redefina sua senha", "reset-password-email", variables);
    }

	@Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity token = passwordResetTokenRepository.findById(UUID.fromString(request.getToken()))
                .orElseThrow(() -> new AuthenticationException("Token inválido ou já usado"));

        if (isResetTokenExpired(token)) {
            throw new AuthenticationException("Token expirado");
        }

        UserEntity user = token.getUser();
        user.setPassword(encoder.encode(request.getNewPassword()));

        userService.save(user);
        passwordResetTokenRepository.delete(token);
        tokenRevocationService.revokeAllTokensFromUser(user);
    }
    
    private boolean isResetTokenExpired(PasswordResetTokenEntity token) {
        return token.getExpiresAt().isBefore(Instant.now());
    }

	public void deleteAccount() {
        userService.deleteAccount();
	}
	
	@Transactional
    public SessionTransferResponse createSessionTransferToken(String target) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("Não autenticado");
        }

        UserEntity userDetails = AuthenticatedUserUtils.requireEnrichedUser();

        Instant now = Instant.now();

        SessionTransferTokenEntity token = new SessionTransferTokenEntity();
        token.setId(UUID.randomUUID());
        token.setUser(userDetails);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(Duration.ofMinutes(2)));
        token.setUsed(false);
        token.setTarget(target);

        sessionTransferTokenRepository.save(token);
        
        SessionTransferResponse response = new SessionTransferResponse();
        response.setTransferToken(token.getId().toString());

        return response;
    }
    
    @Transactional
    public ResponseEntity<?> exchangeSession(String transferToken,
    		HttpServletResponse response,
            HttpServletRequest request) {
        SessionTransferTokenEntity token = sessionTransferTokenRepository
                .findById(UUID.fromString(transferToken))
                .orElseThrow(() -> 
                    new AuthenticationException("Token inválido")
                );

        // TODO: Criar scheduler para remover tokens espirados e usados após um delay
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationException("Token inválido ou expirado");
        }

        token.setUsed(true);
        sessionTransferTokenRepository.save(token);

        TokenResponse tokens = this.generateAuthenticationTokens(response, request, token.getUser());

        return AuthorizationUtils.buildTokenResponse(request, tokens);
    }

}
