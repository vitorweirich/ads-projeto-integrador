package com.github.fileshare.services;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.constants.JwtClaims;
import com.github.fileshare.dto.response.MfaJwtResponse;
import com.github.fileshare.dto.response.TokenResponse;
import com.github.fileshare.exceptions.AuthenticationException;
import com.github.fileshare.exceptions.MfaException;
import com.github.fileshare.security.JwtUtils;
import com.github.fileshare.utils.AuthenticatedUserUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import io.jsonwebtoken.Claims;
import io.nayuki.qrcodegen.QrCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final AuthService authService;
    
    public TokenResponse verifyMfa(String bearerToken,
    		int code,
    		HttpServletResponse response,
            HttpServletRequest request) {
        String token = bearerToken.replace("Bearer ", "");

        if (!jwtUtils.validateJwtToken(token)) {
            throw new AuthenticationException("Token inválido ou expirado, por favor reinicie o processo de login");
        }

        Claims claims = jwtUtils.getClaimsFromJwtToken(token);

        if (!Boolean.TRUE.equals(claims.get(JwtClaims.MFA_PENDING, Boolean.class))) {
            throw new MfaException("Token inválido para verificação MFA");
        }

        String email = claims.getSubject();
        String secret = userService.getMfaSecret(email);

        boolean isValid = this.verifyCode(secret, code);
        if (!isValid) {
            throw new MfaException("Código de verificação inválido, verifique o codigo no seu App autenticador e tente novamente");
        }

        UserEntity user = (UserEntity) userService.loadUserByUsername(email);
        
        return authService.generateAuthenticationTokens(response, request, user);

    }

    public MfaJwtResponse buildMfaSetupResponse() {
		UserEntity user = AuthenticatedUserUtils.requireEnrichedUser();
    	
    	if(Boolean.TRUE.equals(user.getMfaVerified())) {
            throw new MfaException("O MFA já está configurado para este usuário. Em caso de perda de acesso, contate um administrador.");
    	}
    	
    	String mfaSetupToken = this.jwtUtils.generateMfaSetupToken(user.getEmail());
    	
    	MfaJwtResponse mfaResponse = MfaJwtResponse.builder().token(mfaSetupToken).type("start_mfa_setup_token").build();
		return mfaResponse;
	}
    
    public byte[] setupMfa(String bearerToken) throws Exception {
        String token = bearerToken.replace("Bearer ", "");

        if (!jwtUtils.validateJwtToken(token)) {
            throw new AuthenticationException("Token inválido ou expirado");
        }

        Claims claims = jwtUtils.getClaimsFromJwtToken(token);

        if (!Boolean.TRUE.equals(claims.get(JwtClaims.MFA_SETUP, Boolean.class))) {
            throw new MfaException("Token não autorizado para configuração do MFA");
        }

        String email = claims.getSubject();

        // Geração de MFA
        String secret = this.generateSecret();
        userService.saveMfaSecret(email, secret);
        String otpAuthUrl = this.getOtpAuthUrl(email, secret);
        return this.generateQrCodeImage(otpAuthUrl);
    }
    
    public void confirmMfaSetup(
            String bearerToken,
            int code) {

        String token = bearerToken.replace("Bearer ", "");

        if (!jwtUtils.validateJwtToken(token)) {
            throw new AuthenticationException("Token inválido ou expirado");
        }

        Claims claims = jwtUtils.getClaimsFromJwtToken(token);

        if (!Boolean.TRUE.equals(claims.get(JwtClaims.MFA_SETUP, Boolean.class))) {
            throw new MfaException("Token não autorizado para confirmação do MFA");
        }

        String email = claims.getSubject();
        String secret = userService.getMfaSecret(email);

        boolean isValid = this.verifyCode(secret, code);
        if (!isValid) {
            throw new MfaException("Código de verificação inválido");
        }
            
        userService.enableMfa(email);
        
        // TODO: Enviar email de confirmacao de MFA usar template: 'mfa-registered'
    }
    
    private boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
    
    private String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    private String getOtpAuthUrl(String username, String secret) {
        String issuer = "App Videos"; // Nome do seu app
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, secret, issuer);
    }

    private byte[] generateQrCodeImage(String otpAuthUrl) {
        QrCode qr = QrCode.encodeText(otpAuthUrl, QrCode.Ecc.MEDIUM);

        String svg = qr.toSvgString(4);

        return svg.getBytes(StandardCharsets.UTF_8);
    }
}
