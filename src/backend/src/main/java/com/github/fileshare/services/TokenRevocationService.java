package com.github.fileshare.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.fileshare.config.entities.RefreshTokenEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.respositories.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

	private final RefreshTokenRepository tokenRepo;
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamilyInNewTransaction(String familyId) {
        List<RefreshTokenEntity> tokens = tokenRepo.findByFamilyId(familyId);
        tokens.forEach(t -> t.setRevoked(true));
        
        // TODO: Receber access_token e incluir ele em uma black_list
        
        tokenRepo.revokeUserTokensByFamilyId(familyId);
    }
	
	public void invalidateToken(String refreshToken) {
		Optional<RefreshTokenEntity> findById = tokenRepo.findById(UUID.fromString(refreshToken));
		
		if(findById.isPresent()) {
			this.revokeFamilyInNewTransaction(findById.get().getFamilyId());
		}
	}

	public void revokeAllTokensFromUser(UserEntity user) {
		tokenRepo.revokeAllUserTokensByUserId(user.getId());
	}
	
}
