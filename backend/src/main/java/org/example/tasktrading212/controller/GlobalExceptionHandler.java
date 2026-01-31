package org.example.tasktrading212.controller;

import org.example.tasktrading212.dto.ErrorResponse;
import org.example.tasktrading212.exceptions.UserAlreadyExistsException;
import org.example.tasktrading212.exceptions.UserNotAuthenticatedException;
import org.example.tasktrading212.exceptions.ZeroBalanceException;
import org.example.tasktrading212.exceptions.ZeroBitcoinAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({ZeroBalanceException.class})
    public ResponseEntity<ErrorResponse> handleBalanceException(Exception e) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error , HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserNotAuthenticatedException.class})
    public ResponseEntity<ErrorResponse> userNotAuthenticated(Exception e) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error , HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> userAlreadyExists(Exception e) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error , HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ZeroBitcoinAvailableException.class})
    public ResponseEntity<ErrorResponse> zeroBitcoinAvailable(Exception e) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(error , HttpStatus.BAD_REQUEST);
    }


}
