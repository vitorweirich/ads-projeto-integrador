package com.github.fileshare.config;

import org.springframework.stereotype.Component;

import com.github.fileshare.respositories.UserRepository;
import com.github.fileshare.utils.AuthenticatedUserUtils;

@Component
public class AuthenticatedUserUtilsConfig {

    public AuthenticatedUserUtilsConfig(UserRepository userRepository) {
    	AuthenticatedUserUtils.setRepository(userRepository);
    }
}
