package com.example.agreement.service;

import com.example.agreement.entity.OtpCode;
import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.OtpType;
import com.example.agreement.exeption.UserAlreadyExistsException;
import com.example.agreement.exeption.UserNotFoundException;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.auth.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    // Step 1: Request OTP for Login
    public void requestLoginOtp(LoginRequestDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        // Check if user exists
        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserNotFoundException("User not found. Please register first.");
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(phoneNumber, OtpType.LOGIN);

        log.info("Login OTP requested for: {}", phoneNumber);
    }

    // Step 2: Verify OTP and Login
    @Transactional
    public AuthResponseDto verifyLoginOtp(VerifyOtpDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        // Verify OTP
        otpService.verifyOtp(phoneNumber, request.getCode(), OtpType.LOGIN);

        // Get user
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate JWT token
        String token = jwtService.generateToken(user);

        log.info("User logged in: {}", phoneNumber);

        return AuthResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    // Step 1: Request OTP for Registration
    public void requestRegistrationOtp(RegistrationRequestDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        // Check if user already exists
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("User with this phone number already exists");
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(phoneNumber, OtpType.REGISTRATION);

        log.info("Registration OTP requested for: {}", phoneNumber);
    }

    // Step 2: Verify OTP and Register
    @Transactional
    public AuthResponseDto verifyRegistrationOtp(VerifyRegistrationDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        // Verify OTP
        otpService.verifyOtp(phoneNumber, request.getCode(), OtpType.REGISTRATION);

        // Check again if user exists
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // Create new user
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.UserRole.USER)
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        log.info("New user registered: {}", phoneNumber);

        return AuthResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // If starts with 998, return as is
        if (cleaned.startsWith("998")) {
            return cleaned;
        }

        // If starts with 0, replace with 998
        if (cleaned.startsWith("0")) {
            return "998" + cleaned.substring(1);
        }

        // Otherwise, add 998
        return "998" + cleaned;
    }
}