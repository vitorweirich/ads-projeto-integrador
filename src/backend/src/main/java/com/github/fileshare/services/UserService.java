package com.github.fileshare.services;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.fileshare.config.admin.AdminEmailAccessEvaluator;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.config.entities.UserSettingsEntity;
import com.github.fileshare.dto.response.StorageDetails;
import com.github.fileshare.dto.response.UserInfoDTO;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.respositories.UploadedFileRepository;
import com.github.fileshare.respositories.UserRepository;
import com.github.fileshare.respositories.UserSettingsRepository;
import com.github.fileshare.utils.AuthenticatedUserUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final AdminEmailAccessEvaluator adminEmailAccessEvaluator;
    
    public ResponseEntity<UserInfoDTO> enrichUser() {
    	Optional<UserEntity> enrichedUser = AuthenticatedUserUtils.getEnrichedUser();
    	
    	if (enrichedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    	UserEntity user = enrichedUser.get();
    	
    	UserInfoDTO userInfoDTO = new UserInfoDTO(user, adminEmailAccessEvaluator.hasAccess(user));
    	
    	// TODO: Fazer em paralelo
    	UserSettingsEntity userSettings = userSettingsRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Unable to retrieve user settings!"));
    	Long usedQuota = uploadedFileRepository.sumSizeByUserId(user.getId()).orElse(0L);
    	
    	userInfoDTO.setStorage(
    			new StorageDetails(userSettings.getStorageLimitBytes(), usedQuota)
			);
    	
    	return ResponseEntity.ok(userInfoDTO);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }
    
    public UserEntity findByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }

    @Transactional
    public void saveMfaSecret(String email, String secret) {
        UserEntity user = (UserEntity) loadUserByUsername(email);
        user.setMfaSecret(secret);
        user.setMfaVerified(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String getMfaSecret(String email) {
        try {
            return ((UserEntity) loadUserByUsername(email)).getMfaSecret();
        } catch (UsernameNotFoundException e) {
            return null;
        }
    }

    @Transactional
    public void enableMfa(String email) {
        UserEntity user = (UserEntity) loadUserByUsername(email);
        user.setMfaVerified(true);
        userRepository.save(user);
    }

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public UserEntity save(UserEntity usuario) {
		return userRepository.save(usuario);
	}

	@Transactional
	public void deleteAccount() {
        UserEntity user = AuthenticatedUserUtils.requireEnrichedUser();

        // TODO: Deletar os arquivos do storage para liberar espaço (pode ser async e opcional em caso de falha)
        // OBS: Em caso de implementação de arquivos sem expiração a remoção NÃO pode ser opcional.
        uploadedFileRepository.deleteByUserId(user.getId());
        // Delete the managed entity to ensure JPA cascades/orphanRemoval are applied
        userRepository.delete(user);
	}
}
