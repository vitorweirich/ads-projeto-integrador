package com.github.fileshare.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.constants.JwtClaims;
import com.github.fileshare.constants.PublicPaths;
import com.github.fileshare.utils.AuthorizationUtils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return Stream.of(PublicPaths.PUBLIC_PATHS_MATCHERS)
				.anyMatch(requestMatcher -> requestMatcher.matches(request));
	}

	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Optional<String> optionalJwt = AuthorizationUtils.extractAccessTokenFromRequest(request);
            
            if (optionalJwt.map(jwtUtils::validateJwtToken).orElse(false)) {
            	String jwt = optionalJwt.get();
            	
            	Claims claims = jwtUtils.getClaimsFromJwtToken(jwt);
            	if (Boolean.TRUE.equals(claims.get(JwtClaims.MFA_PENDING, Boolean.class)) ||
                    Boolean.TRUE.equals(claims.get(JwtClaims.MFA_SETUP, Boolean.class))) {
                    // Token válido mas pendente de MFA ou setup - não autentica
                    filterChain.doFilter(request, response);
                    return;
                }

                UserEntity userDetails = new UserEntity();
                userDetails.setEmail(claims.getSubject());
                userDetails.setName(claims.get(JwtClaims.USER_NAME, String.class));
                
                @SuppressWarnings("unchecked")
				List<String> roles = claims.get(JwtClaims.ROLES, List.class);
                userDetails.setRole(roles.get(0));
                
                userDetails.setEnriched(false);
                
                PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Não foi possível autenticar o usuário: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
