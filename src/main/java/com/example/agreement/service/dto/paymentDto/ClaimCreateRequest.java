package com.example.agreement.service.dto.paymentDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClaimCreateRequest {

    private LocalDate periodStart;
}