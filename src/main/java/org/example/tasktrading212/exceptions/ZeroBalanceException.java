package org.example.tasktrading212.exceptions;

public class ZeroBalanceException extends RuntimeException {
    public ZeroBalanceException(String message) {
        super(message);
    }
}
