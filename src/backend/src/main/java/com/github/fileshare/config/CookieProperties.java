package com.github.fileshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieProperties {
    private boolean secure = true;
    private String domain;
    private String sameSite;
}
