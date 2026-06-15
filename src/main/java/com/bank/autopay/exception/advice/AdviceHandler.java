package com.bank.autopay.exception.advice;

import com.bank.autopay.dto.ValidationErrorResponse;
import com.bank.autopay.exception.RuleAlreadyDeletedException;
import com.bank.autopay.exception.RuleNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AdviceHandler {

    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<String> ruleNotFoundException(RuleNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException  ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                System.currentTimeMillis(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuleAlreadyDeletedException.class)
    public ResponseEntity<String> ruleAlreadyDeletedException(RuleAlreadyDeletedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ValidationErrorResponse> handleOptimisticLock(OptimisticLockException e) {
        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Data was modified by another user. Please reload and try again",
                System.currentTimeMillis(),
                Map.of("error", e.getMessage())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
