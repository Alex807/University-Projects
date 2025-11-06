package org.example;

public class IncorrectNumberOfParameters extends RuntimeException {
    public IncorrectNumberOfParameters(String message) {
        super(message);
    }
}
