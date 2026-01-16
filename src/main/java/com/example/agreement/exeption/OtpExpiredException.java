package com.example.agreement.exeption;

public class OtpExpiredException extends RentalException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
