package com.example.agreement.entity.enumerated;

public enum ContractStatus {
    DRAFT,                  // owner yaratgan
    WAITING_RENTER_INFO,    // renter passport+pinfl kiritishi kerak
    WAITING_OWNER_APPROVAL, // renter hammasini kiritdi, owner tasdiqlashi kerak

    APPROVED,               // owner tasdiqladi, activation (prepaid+claim) qilish mumkin

    ACTIVE,
    REJECTED,
    CLOSED,
    LATE,
    SUSPENDED,
    CANCELLED
}

