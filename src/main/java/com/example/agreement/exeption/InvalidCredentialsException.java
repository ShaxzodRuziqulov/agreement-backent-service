package com.example.agreement.exeption;

public class InvalidCredentialsException extends RentalException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
