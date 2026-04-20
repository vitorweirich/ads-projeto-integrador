package com.github.fileshare.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.fileshare.dto.request.InviteRequest;
import com.github.fileshare.dto.request.ListFilesRequestParams;
import com.github.fileshare.dto.request.ListUsersRequestParams;
import com.github.fileshare.dto.response.AdminFileSignedUrl;
import com.github.fileshare.dto.response.CompleteUserDTO;
import com.github.fileshare.dto.response.FileDTO;
import com.github.fileshare.dto.response.WhitelistEntryDTO;
import com.github.fileshare.services.AdminService;
import com.github.fileshare.services.WhitelistService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("@adminEmailAccessEvaluator.hasAccess(authentication)")
@RequiredArgsConstructor
public class AdminController {
	
	private final AdminService adminService;
	private final WhitelistService whitelistService;

	@GetMapping("/users")
    public ResponseEntity<Page<CompleteUserDTO>> listUsers(@Valid @ModelAttribute ListUsersRequestParams params) {

        return ResponseEntity.ok(adminService.listUsers(params));
    }
	
	@PostMapping("/users/{userId}/reset-mfa")
    public ResponseEntity<Void> resetUserMfa(@PathVariable Long userId) {

		adminService.resetUserMfa(userId);
		
        return ResponseEntity.noContent().build();
    }
	
	@GetMapping("/files")
    public ResponseEntity<Page<FileDTO>> listFiles(@Valid @ModelAttribute ListFilesRequestParams params) {

        return ResponseEntity.ok(adminService.listFiles(params));
    }
	
	@GetMapping("/files/{fileId}")
    public ResponseEntity<AdminFileSignedUrl> getFile(@PathVariable Long fileId) {

        return ResponseEntity.ok(adminService.getFile(fileId));
    }
	
	@DeleteMapping("/files/{fileId}")
	public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
		adminService.deleteFile(fileId);
		
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/whitelist/pending")
	public ResponseEntity<Page<WhitelistEntryDTO>> listPendingWhitelist(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int rows) {
		return ResponseEntity.ok(whitelistService.listPending(page, rows));
	}

	@PostMapping("/whitelist/{id}/approve")
	public ResponseEntity<Void> approveWhitelist(@PathVariable Long id) {
		whitelistService.approveAndInvite(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/whitelist/invite")
	public ResponseEntity<Void> inviteEmail(@RequestBody @Valid InviteRequest request) {
		whitelistService.invite(request);
		return ResponseEntity.noContent().build();
	}
	
}
