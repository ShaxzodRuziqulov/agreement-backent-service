package com.example.agreement.service;

import com.example.agreement.entity.User;
import com.example.agreement.entity.enumerated.OtpType;
import com.example.agreement.exeption.UserAlreadyExistsException;
import com.example.agreement.exeption.UserNotFoundException;
import com.example.agreement.repository.UserRepository;
import com.example.agreement.service.dto.auth.*;
import com.example.agreement.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    @Value("${security.otp.expose-in-response:false}")
    private boolean exposeOtpInResponse;

    public MessageResponse requestLoginOtp(LoginRequestDto request) {
        String phoneNumber = PhoneUtils.normalize(request.getPhoneNumber());

        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserNotFoundException("User not found. Please register first.");
        }

        String code = otpService.generateAndSendOtp(phoneNumber, OtpType.LOGIN);

        log.info("Login OTP requested for: {}", phoneNumber);

        return MessageResponse.builder()
                .message("OTP sent successfully to " + request.getPhoneNumber())
                .success(true)
                .otp(exposeOtpInResponse ? code : null)
                .build();
    }

    @Transactional
    public AuthResponseDto verifyLoginOtp(VerifyOtpDto request) {
        String phoneNumber = PhoneUtils.normalize(request.getPhoneNumber());

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

    public MessageResponse requestRegistrationOtp(RegistrationRequestDto request) {
        String phoneNumber = PhoneUtils.normalize(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserAlreadyExistsException("User with this phone number already exists");
        }

        String code = otpService.generateAndSendOtp(phoneNumber, OtpType.REGISTRATION);

        log.info("Registration OTP requested for: {}", phoneNumber);

        return MessageResponse.builder()
                .message("OTP sent successfully to " + request.getPhoneNumber())
                .success(true)
                .otp(exposeOtpInResponse ? code : null)
                .build();
    }

    @Transactional
    public AuthResponseDto verifyRegistrationOtp(VerifyRegistrationDto request) {
        String phoneNumber = PhoneUtils.normalize(request.getPhoneNumber());

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

}
