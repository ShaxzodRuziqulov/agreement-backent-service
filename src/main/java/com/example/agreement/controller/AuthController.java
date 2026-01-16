package com.example.agreement.controller;

import com.example.agreement.service.AuthService;
import com.example.agreement.service.dto.auth.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Step 1: Request OTP for login
     * POST /api/v1/auth/login/request
     */
    @PostMapping("/login/request")
    public ResponseEntity<MessageResponse> requestLoginOtp(@Valid @RequestBody LoginRequestDto request) {
        authService.requestLoginOtp(request);
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("OTP sent successfully to " + request.getPhoneNumber())
                        .success(true)
                        .build()
        );
    }

    /**
     * Step 2: Verify OTP and login
     * POST /api/v1/auth/login/verify
     */
    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponseDto> verifyLoginOtp(@Valid @RequestBody VerifyOtpDto request) {
        AuthResponseDto response = authService.verifyLoginOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 1: Request OTP for registration
     * POST /api/v1/auth/register/request
     */
    @PostMapping("/register/request")
    public ResponseEntity<MessageResponse> requestRegistrationOtp(@Valid @RequestBody RegistrationRequestDto request) {
        authService.requestRegistrationOtp(request);
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("OTP sent successfully to " + request.getPhoneNumber())
                        .success(true)
                        .build()
        );
    }

    /**
     * Step 2: Verify OTP and register
     * POST /api/v1/auth/register/verify
     */
    @PostMapping("/register/verify")
    public ResponseEntity<AuthResponseDto> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationDto request
    ) {
        AuthResponseDto response = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(response);
    }
}