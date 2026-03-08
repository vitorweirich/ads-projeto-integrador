package com.github.fileshare.config.admin;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "admin")
@Getter
@Setter
public class AdminConfigProperties {

	private Set<String> adminEmails = new HashSet<>();
}
