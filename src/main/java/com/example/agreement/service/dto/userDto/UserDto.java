package com.example.agreement.service.dto.userDto;

import com.example.agreement.entity.enumerated.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String pinfl;
    private VerificationStatus pinflStatus;
    private boolean blocked;
    private String blockReason;
    private LocalDateTime blockedAt;
    private String passportFrontPath;
    private String passportBackPath;
}