package com.example.agreement.service.dto.userDto;

import com.example.agreement.entity.enumerated.VerificationStatus;

public record PassportUploadResponseDto(
        Long userId,
        String passportFrontPath,
        String passportBackPath,
        VerificationStatus passportStatus
) {
}

