package com.example.agreement.controller;


import com.example.agreement.entity.User;
import com.example.agreement.service.UserService;
import com.example.agreement.service.dto.auth.MessageResponse;
import com.example.agreement.service.dto.auth.UpdateProfileDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    /**
     * Upload passport front photo
     * POST /api/v1/users/me/passport/front
     */
    @PostMapping("/me/passport/front")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> uploadPassportFront(@RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.uploadPassportFront(file);
        return ResponseEntity.ok(user);
    }

    /**
     * Upload passport back photo
     * POST /api/v1/users/me/passport/back
     */
    @PostMapping("/me/passport/back")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> uploadPassportBack(@RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.uploadPassportBack(file);
        return ResponseEntity.ok(user);
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