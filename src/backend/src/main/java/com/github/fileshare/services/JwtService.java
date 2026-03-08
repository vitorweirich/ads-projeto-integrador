package com.github.fileshare.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.respositories.UserRepository;
import com.github.fileshare.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
	
	private final UserRepository usuarioRepository;
    private final JwtUtils jwtUtils;

	public String generateAccessToken(Long userId) {
		UserEntity user = usuarioRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Cannot find user with given ID"));
		return jwtUtils.generateJwtToken(user);
	}

}
