package com.example.agreement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final OtpService otpService;

    /**
     * Clean up expired OTPs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void cleanupExpiredOtps() {
        log.info("Starting cleanup of expired OTPs");
        otpService.cleanupExpiredOtps();
        log.info("Expired OTPs cleanup completed");
    }
}
