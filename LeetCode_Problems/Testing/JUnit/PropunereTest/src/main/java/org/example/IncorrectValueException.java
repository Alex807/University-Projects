package org.example;

public class IncorrectValueException extends RuntimeException{
    public IncorrectValueException(String message) {
        super(message);
    }
}
