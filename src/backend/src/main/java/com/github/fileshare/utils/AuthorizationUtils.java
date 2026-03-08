package com.github.fileshare.utils;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.github.fileshare.config.JwtProperties;
import com.github.fileshare.dto.response.TokenResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationUtils {
	
	public static final String ACCESS_TOKEN = "access_token";
	public static final String REFRESH_TOKEN = "refresh_token";
	
	private static boolean secure;
    private static String domain;
    private static String sameSite;
    private static JwtProperties jwtProperties;

    public static void setSecure(boolean secure) {
        AuthorizationUtils.secure = secure;
    }

    public static void setDomain(String domain) {
        AuthorizationUtils.domain = domain;
    }

    public static void setSameSite(String sameSite) {
        AuthorizationUtils.sameSite = sameSite;
    }
    
    public static void setJwtProperties(JwtProperties jwtProperties) {
    	AuthorizationUtils.jwtProperties = jwtProperties;
    }

	private AuthorizationUtils() {}
	
	public static Optional<String> extractAccessTokenFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
		
		return extractAccessTokenFromCookie(request);
	}
	
	public static Optional<String> extractAccessTokenFromCookie(HttpServletRequest request) {
		return findCookie(ACCESS_TOKEN, request);
	}
	
	public static Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader("X-Refresh-Token");
        if (StringUtils.hasText(authHeader)) {
            return Optional.of(authHeader);
        }
		
	    return findCookie(REFRESH_TOKEN, request);
	}
	
	public static Optional<String> findCookie(String cookieName, HttpServletRequest request) {
		if (request.getCookies() != null) {
	        for (Cookie cookie : request.getCookies()) {
	            if (cookieName.equals(cookie.getName())) {
	                return Optional.of(cookie.getValue());
	            }
	        }
	    }
	    return Optional.empty();
	}
	
	public static void injectRefreshTokenCookieIntoResponse(String refreshToken, HttpServletResponse response) {
		injectCookieWithDefaultsIntoResponse(REFRESH_TOKEN, refreshToken, jwtProperties.getRefreshTokenExpiration(), response);
	}
	
	public static void injectAccessTokenCookieIntoResponse(String accessToken, HttpServletResponse response) {
        injectCookieWithDefaultsIntoResponse(ACCESS_TOKEN, accessToken, jwtProperties.getAccessTokenExpiration(), response);
	}
	
	public static void injectCookieWithDefaultsIntoResponse(String cookiename, String cookieValue, long maxAge, HttpServletResponse response) {
        ResponseCookie responseCookie = ResponseCookie.from(cookiename, cookieValue)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .domain(domain)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }
	
	public static boolean isHttpOnlyRequest(HttpServletRequest request) {
	    String header = request.getHeader("X-Http-Only");
	    return header == null || !"false".equalsIgnoreCase(header);
	}
	
	public static ResponseEntity<TokenResponse> buildTokenResponse(HttpServletRequest request,
			TokenResponse tokenResponse) {
		if(!AuthorizationUtils.isHttpOnlyRequest(request)) {
			return ResponseEntity.ok()
					.header("X-Client-Action", "refresh-user")
					.body(tokenResponse);
		}

		return ResponseEntity.noContent()
				.header("X-Client-Action", "refresh-user")
				.build();
	}

	
}
