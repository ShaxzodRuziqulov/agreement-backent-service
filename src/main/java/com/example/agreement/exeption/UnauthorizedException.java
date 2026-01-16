package com.example.agreement.exeption;

public class UnauthorizedException extends RentalException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
