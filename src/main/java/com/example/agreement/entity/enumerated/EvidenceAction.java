package com.example.agreement.entity.enumerated;

public enum EvidenceAction {
    CONTRACT_CREATED,

    CONTRACT_SENT_TO_RENTER,     // NEW: owner renterga yubordi
    RENTER_SUBMITTED_INFO,
    OWNER_APPROVED_CONTRACT,

    CONTRACT_ACTIVATED,

    PAYMENT_CLAIM_CREATED,       // claim create bo‘lganda
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_LATE,

    CONTRACT_SUSPENDED,
    CONTRACT_CANCELLED,
    CONTRACT_CLOSED,

    RENTER_IDENTITY_VERIFIED
}
