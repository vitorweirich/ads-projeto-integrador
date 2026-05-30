package com.github.fileshare.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.fileshare.config.JwtProperties;
import com.github.fileshare.config.StorageProperties;
import com.github.fileshare.config.admin.AdminConfigProperties;
import com.github.fileshare.config.entities.EmailWhitelistEntity.Status;
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
import com.github.fileshare.exceptions.PendingApprovalException;
import com.github.fileshare.respositories.EmailWhitelistRepository;
import com.github.fileshare.respositories.PasswordResetTokenRepository;
import com.github.fileshare.respositories.SessionTransferTokenRepository;
import com.github.fileshare.respositories.TemporaryUserRepository;
import com.github.fileshare.respositories.UserRepository;
import com.github.fileshare.respositories.UserSettingsRepository;
import com.github.fileshare.security.JwtUtils;
import com.github.fileshare.utils.AuthenticatedUserUtils;
import com.github.fileshare.utils.AuthorizationUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	AuthenticationManager authenticationManager;
	@Mock
	JwtUtils jwtUtils;
	@Mock
	TemporaryUserRepository temporaryUserRepository;
	@Mock
	UserSettingsRepository userSettingsRepository;
	@Mock
	StorageProperties storageProperties;
	@Mock
	UserService userService;
	@Mock
	PasswordEncoder encoder;
	@Mock
	EmailService emailService;
	@Mock
	TokenRevocationService tokenRevocationService;
	@Mock
	PasswordResetTokenRepository passwordResetTokenRepository;
	@Mock
	RefreshTokenService refreshTokenService;
	@Mock
	SessionTransferTokenRepository sessionTransferTokenRepository;
	@Mock
	WhitelistService whitelistService;
	@Mock
	EmailWhitelistRepository whitelistRepository;
	@Mock
	AdminConfigProperties adminConfigProperties;

	@Mock
	HttpServletRequest request;
	@Mock
	HttpServletResponse response;
	@Mock
	UserRepository userRepository;

	@InjectMocks
	AuthService authService;

	private UserEntity user;
	private RefreshTokenEntity refreshToken;

	@BeforeEach
	void setUp() {
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setAccessTokenExpiration(3600);
		jwtProperties.setRefreshTokenExpiration(86400);

		AuthorizationUtils.setSecure(false);
		AuthorizationUtils.setDomain("localhost");
		AuthorizationUtils.setSameSite("Lax");
		AuthorizationUtils.setJwtProperties(jwtProperties);

		AuthenticatedUserUtils.setRepository(userRepository);

		user = new UserEntity();
		user.setId(1L);
		user.setEmail("user@test.com");
		user.setName("Test User");
		user.setPassword("encoded");
		user.setRole("USER");
		user.setMfaVerified(false);
		user.setEnriched(true); // evita chamada ao userRepository em AuthenticatedUserUtils

		refreshToken = new RefreshTokenEntity();
		refreshToken.setUserId(user.getId());
		refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
	}

	// -------------------------------------------------------------------------
	// authenticateUser
	// -------------------------------------------------------------------------
	@Nested
	class AuthenticateUser {

		private LoginRequest loginRequest;
		private Authentication authentication;

		@BeforeEach
		void setUp() {
			loginRequest = new LoginRequest();
			loginRequest.setEmail("user@test.com");
			loginRequest.setPassword("password");

			authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}

		@Test
		void deveRetornarNoContentQuandoHttpOnly() {
			when(authenticationManager.authenticate(any())).thenReturn(authentication);
			when(jwtUtils.generateJwtToken(user)).thenReturn("access-token");
			when(refreshTokenService.generateRefreshToken(request, user)).thenReturn(refreshToken);
			when(request.getHeader("X-Http-Only")).thenReturn(null); // httpOnly = true

			ResponseEntity<?> result = authService.authenticateUser(loginRequest, response, request);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			assertThat(result.getHeaders().getFirst("X-Client-Action")).isEqualTo("refresh-user");
			assertThat(result.getBody()).isNull();
		}

		@Test
		void deveRetornarTokensNoBodyQuandoNaoHttpOnly() {
			when(authenticationManager.authenticate(any())).thenReturn(authentication);
			when(jwtUtils.generateJwtToken(user)).thenReturn("access-token");
			when(refreshTokenService.generateRefreshToken(request, user)).thenReturn(refreshToken);
			when(request.getHeader("X-Http-Only")).thenReturn("false");

			ResponseEntity<?> result = authService.authenticateUser(loginRequest, response, request);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(result.getHeaders().getFirst("X-Client-Action")).isEqualTo("refresh-user");
			TokenResponse body = (TokenResponse) result.getBody();
			assertThat(body).isNotNull();
			assertThat(body.getAccessToken()).isEqualTo("access-token");
		}

		@Test
		void deveRetornarMfaResponseQuandoMfaAtivo() {
			user.setMfaVerified(true);
			when(authenticationManager.authenticate(any())).thenReturn(authentication);
			when(jwtUtils.generateJwtTokenWithMfaPending(authentication)).thenReturn("mfa-temp-token");

			ResponseEntity<?> result = authService.authenticateUser(loginRequest, response, request);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
			MfaJwtResponse body = (MfaJwtResponse) result.getBody();
			assertThat(body).isNotNull();
			assertThat(body.getToken()).isEqualTo("mfa-temp-token");
			verify(refreshTokenService, never()).generateRefreshToken(any(), any());
		}

		@Test
		void deveChamarAuthenticationManagerComCredenciais() {
			when(authenticationManager.authenticate(any())).thenReturn(authentication);
			when(jwtUtils.generateJwtToken(user)).thenReturn("access-token");
			when(refreshTokenService.generateRefreshToken(request, user)).thenReturn(refreshToken);

			authService.authenticateUser(loginRequest, response, request);

			verify(authenticationManager)
					.authenticate(argThat(token -> token instanceof UsernamePasswordAuthenticationToken
							&& token.getPrincipal().equals("user@test.com")
							&& token.getCredentials().equals("password")));
		}
	}

	// -------------------------------------------------------------------------
	// generateAuthenticationTokens
	// -------------------------------------------------------------------------
	@Nested
	class GenerateAuthenticationTokens {

		@Test
		void deveGerarTokensEInjetarCookies() {
			when(jwtUtils.generateJwtToken(user)).thenReturn("access-token");
			when(refreshTokenService.generateRefreshToken(request, user)).thenReturn(refreshToken);

			TokenResponse result = authService.generateAuthenticationTokens(response, request, user);

			assertThat(result.getAccessToken()).isEqualTo("access-token");
			assertThat(result.getRefreshToken()).isEqualTo(refreshToken.getId().toString());
			verify(jwtUtils).generateJwtToken(user);
			verify(refreshTokenService).generateRefreshToken(request, user);
		}
	}

	// -------------------------------------------------------------------------
	// buildMfaResponseIfExists
	// -------------------------------------------------------------------------
	@Nested
	class BuildMfaResponseIfExists {

		private Authentication authentication;

		@BeforeEach
		void setUp() {
			authentication = new UsernamePasswordAuthenticationToken(user, null);
		}

		@Test
		void deveRetornarNullQuandoMfaNaoAtivo() {
			user.setMfaVerified(false);
			ResponseEntity<MfaJwtResponse> result = authService.buildMfaResponseIfExists(authentication, user);
			assertThat(result).isNull();
		}

		@Test
		void deveRetornarMfaResponseQuandoMfaAtivo() {
			user.setMfaVerified(true);
			when(jwtUtils.generateJwtTokenWithMfaPending(authentication)).thenReturn("mfa-token");

			ResponseEntity<MfaJwtResponse> result = authService.buildMfaResponseIfExists(authentication, user);

			assertThat(result).isNotNull();
			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(result.getBody()).isNotNull();
			assertThat(result.getBody().getToken()).isEqualTo("mfa-token");
			verify(jwtUtils).generateJwtTokenWithMfaPending(authentication);
		}
	}

	// -------------------------------------------------------------------------
	// processUserLogout
	// -------------------------------------------------------------------------
	@Nested
	class ProcessUserLogout {

		@Test
		void deveRetornarNoContentEInvalidarToken() {
			when(request.getHeader("X-Refresh-Token")).thenReturn("refresh-uuid");

			ResponseEntity<Void> result = authService.processUserLogout(request, response);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			verify(tokenRevocationService).invalidateToken("refresh-uuid");
		}

		@Test
		void deveRetornarNoContentSemInvalidarQuandoSemToken() {
			when(request.getHeader("X-Refresh-Token")).thenReturn(null);
			when(request.getCookies()).thenReturn(null);

			ResponseEntity<Void> result = authService.processUserLogout(request, response);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			verify(tokenRevocationService, never()).invalidateToken(any());
		}
	}

	// -------------------------------------------------------------------------
	// registerTemporaryUser
	// -------------------------------------------------------------------------
	@Nested
	class RegisterTemporaryUser {

		private SignupRequest signupRequest;

		@BeforeEach
		void setUp() {
			signupRequest = new SignupRequest();
			signupRequest.setEmail("novo@test.com");
			signupRequest.setName("Novo User");
			signupRequest.setPassword("senha123");
			ReflectionTestUtils.setField(authService, "whitelistEnabled", true);
			ReflectionTestUtils.setField(authService, "expirationMinutes", 60L);
			ReflectionTestUtils.setField(authService, "confirmationBaseUrl", "http://localhost/confirm");
		}

		@Test
		void deveConsumirInviteTokenQuandoInvitePresente() throws Exception {
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(false);
			when(encoder.encode(signupRequest.getPassword())).thenReturn("encoded");
			when(temporaryUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

			authService.registerTemporaryUser(signupRequest, "invite-token");

			verify(whitelistService).consumeInviteToken("invite-token");
			verify(whitelistService, never()).checkEmailStatus(any());
		}

		@Test
		void deveLancarPendingApprovalQuandoEmailNaoNaWhitelist() {
			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(null);

			assertThatThrownBy(() -> authService.registerTemporaryUser(signupRequest, null))
					.isInstanceOf(PendingApprovalException.class);

			verify(whitelistService).registerPendingEmail(signupRequest.getEmail(), signupRequest.getName());
		}

		@Test
		void deveLancarPendingApprovalQuandoStatusPending() {
			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(Status.PENDING);

			assertThatThrownBy(() -> authService.registerTemporaryUser(signupRequest, null))
					.isInstanceOf(PendingApprovalException.class);
		}

		@Test
		void deveLancarExcecaoQuandoEmailDefinitivoJaExiste() {
			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(Status.ALLOWED);
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(true);

			assertThatThrownBy(() -> authService.registerTemporaryUser(signupRequest, null))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("já está em uso");
		}

		@Test
		void deveLancarExcecaoQuandoEmailTemporarioNaoExpirado() {
			TemporaryUserEntity tempUser = new TemporaryUserEntity();
			tempUser.setEmail(signupRequest.getEmail());
			tempUser.setExpiresAt(ZonedDateTime.now().plusHours(1));

			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(Status.ALLOWED);
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(tempUser));
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(false);

			assertThatThrownBy(() -> authService.registerTemporaryUser(signupRequest, null))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("já está em uso");
		}

		@Test
		void deveSalvarEEnviarEmailDeConfirmacao() throws Exception {
			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(Status.ALLOWED);
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(false);
			when(encoder.encode(signupRequest.getPassword())).thenReturn("encoded");
			when(temporaryUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

			authService.registerTemporaryUser(signupRequest, null);

			verify(temporaryUserRepository).save(argThat(t -> t.getEmail().equals(signupRequest.getEmail())
					&& t.getName().equals(signupRequest.getName()) && t.getPassword().equals("encoded")));
			verify(emailService).sendHtmlEmailFromTemplate(eq(signupRequest.getEmail()), any(),
					eq("magic-link-confirmation"), any());
		}

		@Test
		void deveLancarMessageFeedbackExceptionQuandoEmailFalha() throws Exception {
			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(Status.ALLOWED);
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(false);
			when(encoder.encode(any())).thenReturn("encoded");
			when(temporaryUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));
			doThrow(new jakarta.mail.MessagingException("smtp error")).when(emailService)
					.sendHtmlEmailFromTemplate(any(), any(), any(), any());

			assertThatThrownBy(() -> authService.registerTemporaryUser(signupRequest, null))
					.isInstanceOf(MessageFeedbackException.class);
		}

		@Test
		void devePermitirAdminEmailSemWhitelist() throws Exception {
			ReflectionTestUtils.setField(authService, "whitelistEnabled", true);
			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of(signupRequest.getEmail()));
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(false);
			when(encoder.encode(any())).thenReturn("encoded");
			when(temporaryUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

			authService.registerTemporaryUser(signupRequest, null);

			verify(whitelistService, never()).checkEmailStatus(any());
			verify(temporaryUserRepository).save(any());
		}

		@Test
		void deveReutilizarTemporaryUserExpirado() throws Exception {
			TemporaryUserEntity tempUser = new TemporaryUserEntity();
			tempUser.setEmail(signupRequest.getEmail());
			tempUser.setExpiresAt(ZonedDateTime.now().minusHours(1)); // expirado

			when(adminConfigProperties.getAdminEmails()).thenReturn(java.util.Set.of());
			when(whitelistService.checkEmailStatus(signupRequest.getEmail())).thenReturn(Status.ALLOWED);
			when(temporaryUserRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(tempUser));
			when(userService.existsByEmail(signupRequest.getEmail())).thenReturn(false);
			when(encoder.encode(any())).thenReturn("encoded");
			when(temporaryUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));

			authService.registerTemporaryUser(signupRequest, null);

			// deve reutilizar o mesmo objeto (não criar novo)
			verify(temporaryUserRepository).save(tempUser);
		}
	}

	// -------------------------------------------------------------------------
	// confirmEmailAndSavePermanentUser
	// -------------------------------------------------------------------------
	@Nested
	class ConfirmEmailAndSavePermanentUser {

		private TemporaryUserEntity tempUser;

		@BeforeEach
		void setUp() {
			tempUser = new TemporaryUserEntity();
			tempUser.setEmail("novo@test.com");
			tempUser.setName("Novo User");
			tempUser.setPassword("encoded");
			tempUser.setExpiresAt(ZonedDateTime.now().plusHours(1));
		}

		@Test
		void deveLancarExcecaoQuandoTokenInvalido() {
			when(temporaryUserRepository.findByConfirmationToken("bad-token")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.confirmEmailAndSavePermanentUser("bad-token"))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("inválido ou expirado");
		}

		@Test
		void deveLancarExcecaoQuandoTokenExpirado() {
			tempUser.setExpiresAt(ZonedDateTime.now().minusMinutes(1));
			when(temporaryUserRepository.findByConfirmationToken("token")).thenReturn(Optional.of(tempUser));

			assertThatThrownBy(() -> authService.confirmEmailAndSavePermanentUser("token"))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("Token expirado");

			verify(temporaryUserRepository).delete(tempUser);
		}

		@Test
		void deveLancarExcecaoQuandoEmailJaConfirmado() {
			when(temporaryUserRepository.findByConfirmationToken("token")).thenReturn(Optional.of(tempUser));
			when(userService.existsByEmail(tempUser.getEmail())).thenReturn(true);

			assertThatThrownBy(() -> authService.confirmEmailAndSavePermanentUser("token"))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("já foi confirmado");

			verify(temporaryUserRepository).delete(tempUser);
		}

		@Test
		void deveSalvarUserESettingsEDeletarTemporario() {
			when(temporaryUserRepository.findByConfirmationToken("token")).thenReturn(Optional.of(tempUser));
			when(userService.existsByEmail(tempUser.getEmail())).thenReturn(false);
			when(userService.save(any())).thenAnswer(i -> {
				UserEntity u = i.getArgument(0);
				u.setId(10L);
				return u;
			});
			when(storageProperties.getDefaultMaxStorageUsagePerUser()).thenReturn(1073741824L);
			when(storageProperties.getDefaultMaxFileRetentionDays()).thenReturn(30);
			when(whitelistRepository.findByEmail(tempUser.getEmail())).thenReturn(Optional.empty());

			authService.confirmEmailAndSavePermanentUser("token");

			verify(userService).save(argThat(u -> u.getEmail().equals(tempUser.getEmail())
					&& u.getName().equals(tempUser.getName()) && u.getRole().equals("USER")));
			verify(userSettingsRepository).save(any(UserSettingsEntity.class));
			verify(temporaryUserRepository).delete(tempUser);
		}
	}

	// -------------------------------------------------------------------------
	// generateTokenAndSendEmail (forgot password)
	// -------------------------------------------------------------------------
	@Nested
	class GenerateTokenAndSendEmail {

		@Test
		void deveSalvarTokenEEnviarEmail() throws Exception {
			ForgotPasswordRequest req = new ForgotPasswordRequest();
			req.setEmail("user@test.com");
			ReflectionTestUtils.setField(authService, "resetPasswordBaseUrl", "http://localhost/reset");

			when(userService.findByEmail("user@test.com")).thenReturn(user);
			when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

			authService.generateTokenAndSendEmail(req);

			verify(passwordResetTokenRepository)
					.save(argThat(t -> t.getUser().equals(user) && t.getExpiresAt().isAfter(Instant.now())));
			verify(emailService).sendHtmlEmailFromTemplate(eq("user@test.com"), any(), eq("reset-password-email"),
					any());
		}
	}

	// -------------------------------------------------------------------------
	// resetPassword
	// -------------------------------------------------------------------------
	@Nested
	class ResetPassword {

		private PasswordResetTokenEntity resetToken;

		@BeforeEach
		void setUp() {
			resetToken = new PasswordResetTokenEntity();
			resetToken.setId(UUID.randomUUID());
			resetToken.setUser(user);
			resetToken.setCreatedAt(Instant.now());
			resetToken.setExpiresAt(Instant.now().plusSeconds(1800));
		}

		@Test
		void deveLancarExcecaoQuandoTokenNaoEncontrado() {
			UUID id = UUID.randomUUID();
			ResetPasswordRequest req = new ResetPasswordRequest();
			req.setToken(id.toString());
			req.setNewPassword("newpass");

			when(passwordResetTokenRepository.findById(id)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.resetPassword(req)).isInstanceOf(AuthenticationException.class)
					.hasMessageContaining("inválido ou já usado");
		}

		@Test
		void deveLancarExcecaoQuandoTokenExpirado() {
			resetToken.setExpiresAt(Instant.now().minusSeconds(1));
			ResetPasswordRequest req = new ResetPasswordRequest();
			req.setToken(resetToken.getId().toString());
			req.setNewPassword("newpass");

			when(passwordResetTokenRepository.findById(resetToken.getId())).thenReturn(Optional.of(resetToken));

			assertThatThrownBy(() -> authService.resetPassword(req)).isInstanceOf(AuthenticationException.class)
					.hasMessageContaining("expirado");
		}

		@Test
		void deveRedefinirSenhaEDeletarToken() {
			ResetPasswordRequest req = new ResetPasswordRequest();
			req.setToken(resetToken.getId().toString());
			req.setNewPassword("newpass");

			when(passwordResetTokenRepository.findById(resetToken.getId())).thenReturn(Optional.of(resetToken));
			when(encoder.encode("newpass")).thenReturn("encoded-new");
			when(userService.save(any())).thenReturn(user);

			authService.resetPassword(req);

			assertThat(user.getPassword()).isEqualTo("encoded-new");
			verify(userService).save(user);
			verify(passwordResetTokenRepository).delete(resetToken);
			verify(tokenRevocationService).revokeAllTokensFromUser(user);
		}
	}

	// -------------------------------------------------------------------------
	// deleteAccount
	// -------------------------------------------------------------------------
	@Nested
	class DeleteAccount {

		@Test
		void deveDelegarParaUserService() {
			authService.deleteAccount();
			verify(userService).deleteAccount();
		}
	}

	// -------------------------------------------------------------------------
	// createSessionTransferToken
	// -------------------------------------------------------------------------
	@Nested
	class CreateSessionTransferToken {

		@Test
		void deveCriarTokenERetornarResponse() {
			org.springframework.security.core.context.SecurityContextHolder.getContext()
					.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

			when(sessionTransferTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

			SessionTransferResponse result = authService.createSessionTransferToken("WEB");

			assertThat(result.getTransferToken()).isNotNull();
			verify(sessionTransferTokenRepository).save(argThat(t -> t.getUser().equals(user) && !t.isUsed()
					&& t.getTarget().equals("WEB") && t.getExpiresAt().isAfter(Instant.now())));

			org.springframework.security.core.context.SecurityContextHolder.clearContext();
		}

		@Test
		void deveLancarExcecaoQuandoNaoAutenticado() {
			org.springframework.security.core.context.SecurityContextHolder.clearContext();

			assertThatThrownBy(() -> authService.createSessionTransferToken("WEB"))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("Não autenticado");
		}
	}

	// -------------------------------------------------------------------------
	// exchangeSession
	// -------------------------------------------------------------------------
	@Nested
	class ExchangeSession {

		private SessionTransferTokenEntity transferToken;

		@BeforeEach
		void setUp() {
			transferToken = new SessionTransferTokenEntity();
			transferToken.setId(UUID.randomUUID());
			transferToken.setUser(user);
			transferToken.setCreatedAt(Instant.now());
			transferToken.setExpiresAt(Instant.now().plusSeconds(120));
			transferToken.setUsed(false);
			transferToken.setTarget("WEB");
		}

		@Test
		void deveLancarExcecaoQuandoTokenNaoEncontrado() {
			UUID id = UUID.randomUUID();
			when(sessionTransferTokenRepository.findById(id)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.exchangeSession(id.toString(), response, request))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("inválido");
		}

		@Test
		void deveLancarExcecaoQuandoTokenJaUsado() {
			transferToken.setUsed(true);
			when(sessionTransferTokenRepository.findById(transferToken.getId())).thenReturn(Optional.of(transferToken));

			assertThatThrownBy(() -> authService.exchangeSession(transferToken.getId().toString(), response, request))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("inválido ou expirado");
		}

		@Test
		void deveLancarExcecaoQuandoTokenExpirado() {
			transferToken.setExpiresAt(Instant.now().minusSeconds(1));
			when(sessionTransferTokenRepository.findById(transferToken.getId())).thenReturn(Optional.of(transferToken));

			assertThatThrownBy(() -> authService.exchangeSession(transferToken.getId().toString(), response, request))
					.isInstanceOf(AuthenticationException.class).hasMessageContaining("inválido ou expirado");
		}

		@Test
		void deveRetornarNoContentQuandoHttpOnlyEMarcarTokenComoUsado() {
			when(sessionTransferTokenRepository.findById(transferToken.getId())).thenReturn(Optional.of(transferToken));
			when(jwtUtils.generateJwtToken(user)).thenReturn("access-token");
			when(refreshTokenService.generateRefreshToken(request, user)).thenReturn(refreshToken);
			when(request.getHeader("X-Http-Only")).thenReturn(null);

			ResponseEntity<?> result = authService.exchangeSession(transferToken.getId().toString(), response, request);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			assertThat(transferToken.isUsed()).isTrue();
			verify(sessionTransferTokenRepository).save(transferToken);
		}

		@Test
		void deveRetornarTokensNoBodyQuandoNaoHttpOnly() {
			when(sessionTransferTokenRepository.findById(transferToken.getId())).thenReturn(Optional.of(transferToken));
			when(jwtUtils.generateJwtToken(user)).thenReturn("access-token");
			when(refreshTokenService.generateRefreshToken(request, user)).thenReturn(refreshToken);
			when(request.getHeader("X-Http-Only")).thenReturn("false");

			ResponseEntity<?> result = authService.exchangeSession(transferToken.getId().toString(), response, request);

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
			TokenResponse body = (TokenResponse) result.getBody();
			assertThat(body).isNotNull();
			assertThat(body.getAccessToken()).isEqualTo("access-token");
		}
	}
}
