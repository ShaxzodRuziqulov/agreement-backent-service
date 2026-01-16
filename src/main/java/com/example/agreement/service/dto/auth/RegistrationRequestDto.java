package com.example.agreement.service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDto {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+998|998|0)?[0-9]{9}$", message = "Invalid phone number format")
    private String phoneNumber;
}
