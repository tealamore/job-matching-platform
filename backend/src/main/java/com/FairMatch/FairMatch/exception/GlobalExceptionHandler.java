package com.FairMatch.FairMatch.exception;

import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ MethodArgumentNotValidException.class, BadRequestException.class })
    public ResponseEntity<Map<String, String>> handleValidationExceptions(Exception ex) {
      return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(exception = {UsernameNotFoundException.class, PermissionDeniedDataAccessException.class})
    public ResponseEntity<Void> handle401Exceptions(Exception ex) {
      System.out.println(ex.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGeneralException(Exception ex) {
      System.out.println("Failed because: " + ex.getMessage());
      System.out.println(Arrays.toString(ex.getStackTrace()));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

