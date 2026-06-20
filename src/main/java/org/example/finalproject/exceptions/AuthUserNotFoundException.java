package org.example.finalproject.exceptions;

public class AuthUserNotFoundException extends RuntimeException {
    public AuthUserNotFoundException(String message) {
        super(message);
    }
}
