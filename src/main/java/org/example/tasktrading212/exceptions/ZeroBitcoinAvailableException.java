package org.example.tasktrading212.exceptions;

public class ZeroBitcoinAvailableException extends RuntimeException {
    public ZeroBitcoinAvailableException(String message) {
        super(message);
    }
}
