package com.example.agreement.exeption;

public class TooManyAttemptsException extends RentalException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}
