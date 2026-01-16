package com.example.agreement.exeption;

public class ContractNotFoundException extends RentalException {
    public ContractNotFoundException(String message) {
        super(message);
    }
}
