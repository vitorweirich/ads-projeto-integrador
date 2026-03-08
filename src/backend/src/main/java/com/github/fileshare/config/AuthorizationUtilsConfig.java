package com.github.fileshare.config;

import org.springframework.stereotype.Component;

import com.github.fileshare.utils.AuthorizationUtils;

@Component
public class AuthorizationUtilsConfig {

    public AuthorizationUtilsConfig(CookieProperties cookieProperties, JwtProperties jwtProperties) {
        AuthorizationUtils.setSecure(cookieProperties.isSecure());
        AuthorizationUtils.setDomain(cookieProperties.getDomain());
        AuthorizationUtils.setSameSite(cookieProperties.getSameSite());
        
        AuthorizationUtils.setJwtProperties(jwtProperties);
    }
}
