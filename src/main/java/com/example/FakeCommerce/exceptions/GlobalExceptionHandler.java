package com.example.FakeCommerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.FakeCommerce.utils.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "Resource not found"));
    }

    @ExceptionHandler(ResourceDeletionException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceDeletionException(ResourceDeletionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "Delete operation failed"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName()
                + "'. Expected type: " + ex.getRequiredType().getSimpleName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, "Invalid request parameter"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllGeneralExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Something went wrong", "Internal server error"));
    }

}
