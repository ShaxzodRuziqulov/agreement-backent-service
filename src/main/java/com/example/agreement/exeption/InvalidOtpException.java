package com.example.agreement.exeption;

public class InvalidOtpException extends RentalException {
    public InvalidOtpException(String message) {
        super(message);
    }
}