package org.example.finalproject.exceptions;

public class ActiveUserNotFoundException extends RuntimeException {
    public ActiveUserNotFoundException(String message) {
        super(message);
    }
}
