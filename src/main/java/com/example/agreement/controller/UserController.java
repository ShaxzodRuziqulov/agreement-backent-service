package com.example.agreement.controller;


import com.example.agreement.entity.User;
import com.example.agreement.service.UserService;
import com.example.agreement.service.dto.auth.MessageResponse;
import com.example.agreement.service.dto.auth.UpdateProfileDto;
import com.example.agreement.service.dto.userDto.PassportUploadResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * Update user profile
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
        User updatedUser = userService.updateProfile(dto);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping(value = "/me/passport", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PassportUploadResponseDto> uploadPassport(@RequestParam("side") String side,
                                                                    @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userService.uploadPassport(file, side));
    }

    /**
     * Delete account
     * DELETE /api/v1/users/me
     */
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Account deleted successfully")
                        .success(true)
                        .build()
        );
    }

    /**
     * Get user by ID (Admin only)
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}