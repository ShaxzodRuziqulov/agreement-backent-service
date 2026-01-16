package com.example.agreement.exeption;

public class UserAlreadyExistsException extends RentalException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
