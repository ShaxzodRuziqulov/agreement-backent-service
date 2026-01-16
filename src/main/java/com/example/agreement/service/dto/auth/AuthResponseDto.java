package com.example.agreement.service.dto.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private Long userId;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String role;
    private String message;
}