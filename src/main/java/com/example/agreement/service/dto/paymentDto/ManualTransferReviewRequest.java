package com.example.agreement.service.dto.paymentDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualTransferReviewRequest {
    private boolean approve;
    private String reviewNote;
}
