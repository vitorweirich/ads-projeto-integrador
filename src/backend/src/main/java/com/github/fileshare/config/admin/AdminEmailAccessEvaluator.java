package com.github.fileshare.config.admin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.internal.SimplifiedUser;

import lombok.RequiredArgsConstructor;

@Component("adminEmailAccessEvaluator")
@RequiredArgsConstructor
public class AdminEmailAccessEvaluator {
	
	private final AdminConfigProperties adminConfigProperties;

	public boolean hasAccess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof UserEntity) {
            email = ((UserEntity) principal).getEmail();
        }

        return isAdminEmail(email);
    }

	public boolean hasAccess(SimplifiedUser user) {
        if (user == null) {
            return false;
        }

        String email = user.getEmail();


        return isAdminEmail(email);
    }
	
	private boolean isAdminEmail(String email) {
		return email != null && adminConfigProperties.getAdminEmails().contains(email);
	}
	
}
