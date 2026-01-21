package com.example.agreement.service;

import com.example.agreement.entity.OtpCode;
import com.example.agreement.entity.enumerated.OtpType;
import com.example.agreement.exeption.InvalidOtpException;
import com.example.agreement.exeption.OtpExpiredException;
import com.example.agreement.exeption.TooManyAttemptsException;
import com.example.agreement.repository.SmsOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final SmsOtpRepository otpRepository;
    private final StringRedisTemplate redisTemplate;
    private final SmsService smsService;

    private final SecureRandom random = new SecureRandom();

    @Value("${security.otp.expiration-time}")
    private long otpExpirationTime;

    @Value("${security.otp.length}")
    private int otpLength;

    @Value("${security.otp.max-attempts}")
    private int maxAttempts;

    private String normalizePhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("998")) return cleaned;
        if (cleaned.startsWith("0")) return "998" + cleaned.substring(1);
        return "998" + cleaned;
    }

    @Transactional
    public void generateAndSendOtp(String phoneNumber, OtpType type) {

        phoneNumber = normalizePhoneNumber(phoneNumber); // ✅ ADD

        String rateLimitKey = "otp:ratelimit:" + phoneNumber;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new TooManyAttemptsException("Please wait before requesting a new OTP");
        }

        otpRepository.invalidatePreviousOtps(phoneNumber, type);

        String code = generateOtpCode();
        String codeHash = BCrypt.hashpw(code, BCrypt.gensalt());

        OtpCode otp = OtpCode.builder()
                .phoneNumber(phoneNumber) // ✅ normalized bo'ladi
                .type(type)
                .codeHash(codeHash)
                .attempts(0)
                .used(false)
                .expiresAt(LocalDateTime.now().plusSeconds(otpExpirationTime))
                .build();

        otpRepository.save(otp);

        redisTemplate.opsForValue()
                .set(rateLimitKey, "1", 60, TimeUnit.SECONDS);

        smsService.sendOtp(phoneNumber, code);

        log.info("OTP generated for {}", phoneNumber);
    }


    @Transactional
    public void verifyOtp(String phoneNumber, String code, OtpType type) {

        phoneNumber = normalizePhoneNumber(phoneNumber); // ✅ ADD

        OtpCode otp = otpRepository.findLatestValidOtp(phoneNumber, type)
                .orElseThrow(() -> new InvalidOtpException("No valid OTP found"));

        if (otp.isExpired()) {
            throw new OtpExpiredException("OTP has expired");
        }

        if (otp.getUsed()) {
            throw new InvalidOtpException("OTP already used");
        }

        if (otp.getAttempts() >= maxAttempts) {
            throw new TooManyAttemptsException("Maximum verification attempts exceeded");
        }

        otp.setAttempts(otp.getAttempts() + 1);

        if (!BCrypt.checkpw(code, otp.getCodeHash())) {
            otpRepository.save(otp);
            throw new InvalidOtpException("Invalid OTP code");
        }

        otp.setUsed(true);
        otpRepository.save(otp);

        log.info("OTP verified for {}", phoneNumber);
    }


    private String generateOtpCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
