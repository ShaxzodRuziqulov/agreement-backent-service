package com.example.agreement.service.dto.userDto;

import com.example.agreement.entity.enumerated.UserBlockType;
import com.example.agreement.entity.enumerated.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private UserBlockType blockType;
    private VerificationStatus passportStatus;
    private VerificationStatus pinflStatus;
}

