package com.github.fileshare.controllers;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.fileshare.dto.request.ForgotPasswordRequest;
import com.github.fileshare.dto.request.LoginRequest;
import com.github.fileshare.dto.request.MagicLoginRequest;
import com.github.fileshare.dto.request.ResetPasswordRequest;
import com.github.fileshare.dto.request.SignupRequest;
import com.github.fileshare.dto.response.MessageResponse;
import com.github.fileshare.dto.response.MfaJwtResponse;
import com.github.fileshare.dto.response.TokenResponse;
import com.github.fileshare.dto.response.UserInfoDTO;
import com.github.fileshare.services.AuthService;
import com.github.fileshare.services.MagicLinkService;
import com.github.fileshare.services.MfaService;
import com.github.fileshare.services.RefreshTokenService;
import com.github.fileshare.services.UserService;
import com.github.fileshare.utils.AuthorizationUtils;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final MagicLinkService magicLinkService;
    private final RefreshTokenService tokenService;
    private final MfaService mfaService;
    private final AuthService authService;
    private final UserService userService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Sempre retorna 200 OK por segurança, mesmo que o e-mail não exista
        try {
        	authService.generateTokenAndSendEmail(request);
        } catch (Exception ignored) {}
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    	authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> me() {
        return userService.enrichUser();
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletResponse response,
                                              HttpServletRequest request) {

        return authService.authenticateUser(loginRequest, response, request);
    }
	
    
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request,
                                                 HttpServletResponse response) {
        TokenResponse tokenResponse = tokenService.rotateToken(request, response);
        
        return AuthorizationUtils.buildTokenResponse(request, tokenResponse);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    	this.authService.processUserLogout(request, response);

        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/mfa/setup/initiate")
    public ResponseEntity<MfaJwtResponse> startSetupMfa() {
    	MfaJwtResponse mfaResponse = this.mfaService.buildMfaSetupResponse();

        return ResponseEntity.ok(mfaResponse);
    }

    @PostMapping(value = "/mfa/setup", produces = "image/svg+xml")
    public ResponseEntity<byte[]> setupMfa(@RequestHeader("Authorization") String bearerToken) throws Exception {
        byte[] qrImage = mfaService.setupMfa(bearerToken);

        return ResponseEntity.ok(qrImage);
    }
    
    @PostMapping("/mfa/confirm")
    public ResponseEntity<MessageResponse> confirmMfaSetup(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam int code) {

    	mfaService.confirmMfaSetup(bearerToken, code);

        return ResponseEntity.ok(new MessageResponse("MFA ativado com sucesso"));
    }
    
    @PostMapping("/mfa/verify")
    public ResponseEntity<TokenResponse> verifyMfa(@RequestHeader("Authorization") String bearerToken, @RequestParam int code,
    		HttpServletResponse response,
            HttpServletRequest request) {
        
        TokenResponse tokenResponse = mfaService.verifyMfa(bearerToken, code, response, request);

		return AuthorizationUtils.buildTokenResponse(request, tokenResponse);
    }
    
    @PostMapping("/login/generate-magic-link")
    public ResponseEntity<Void> generateMagicLink(@Valid @RequestBody MagicLoginRequest request) throws MessagingException, IOException {
    	magicLinkService.generateAndSend(request.getEmail());
    	
    	return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/login/magic-link")
    public ResponseEntity<TokenResponse> authenticateUserWithMagicLink(@RequestParam String token,
    		HttpServletResponse response,
            HttpServletRequest request) {
        
        TokenResponse tokenResponse = magicLinkService.authenticateUserWithMagicLink(token, response, request);
        
        return AuthorizationUtils.buildTokenResponse(request, tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        this.authService.registerTemporaryUser(signUpRequest);
        
        return ResponseEntity.ok(new MessageResponse("Cadastro iniciado! Confirme seu e-mail."));
    }

    @GetMapping("/confirm/{token}")
    public ResponseEntity<MessageResponse> confirmEmail(@PathVariable String token) {
        this.authService.confirmEmailAndSavePermanentUser(token);
    	
        return ResponseEntity.ok(new MessageResponse("Usuário confirmado e registrado com sucesso!"));
    }
    
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/account")
    public ResponseEntity<MessageResponse> deleteAccount() {
    	// TODO: Se MFA estiver habilitado, exigir codigo OU gerar um fluxo de exclusão via envio de email?
        this.authService.deleteAccount();
    	
        return ResponseEntity.ok(new MessageResponse("Usuário removido com sucesso!"));
    }
}
