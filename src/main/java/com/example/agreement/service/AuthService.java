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

    public void requestLoginOtp(LoginRequestDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserNotFoundException("User not found. Please register first.");
        }

        otpService.generateAndSendOtp(phoneNumber, OtpType.LOGIN);

        log.info("Login OTP requested for: {}", phoneNumber);
    }

    @Transactional
    public AuthResponseDto verifyLoginOtp(VerifyOtpDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        otpService.verifyOtp(phoneNumber, request.getCode(), OtpType.LOGIN);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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

    public void requestRegistrationOtp(RegistrationRequestDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("User with this phone number already exists");
        }

        otpService.generateAndSendOtp(phoneNumber, OtpType.REGISTRATION);

        log.info("Registration OTP requested for: {}", phoneNumber);
    }

    @Transactional
    public AuthResponseDto verifyRegistrationOtp(VerifyRegistrationDto request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        otpService.verifyOtp(phoneNumber, request.getCode(), OtpType.REGISTRATION);

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .phoneNumber(phoneNumber)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.UserRole.USER)
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

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
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("998")) {
            return cleaned;
        }

        if (cleaned.startsWith("0")) {
            return "998" + cleaned.substring(1);
        }

        return "998" + cleaned;
    }
}