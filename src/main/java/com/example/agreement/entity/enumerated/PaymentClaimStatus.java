package com.example.agreement.entity.enumerated;


public enum PaymentClaimStatus {
    CLAIMED,     // owner chiqardi: shu oy uchun to'lov kerak
    CONFIRMED,   // owner: ha pul tushdi
    REJECTED,    // owner: yo'q, pul tushmadi / noto'g'ri

    OPEN,
    PARTIALLY_PAID,
    PAID,
    CANCELLED
}
