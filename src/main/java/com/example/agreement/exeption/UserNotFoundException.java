package com.example.agreement.exeption;

public class UserNotFoundException extends RentalException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
