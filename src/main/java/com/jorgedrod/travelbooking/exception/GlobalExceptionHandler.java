package com.jorgedrod.travelbooking.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import com.jorgedrod.travelbooking.dto.ApiError;

import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {

        ApiError apiError = new ApiError();
        apiError.setStatus(400);
        apiError.setError("Bad Request");


        List<String> messages = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            messages.add(error.getField() + ": " + error.getDefaultMessage());
        }

        apiError.setMessage(String.join("; ", messages));

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(InvalidBookingDateException.class)
    public ResponseEntity<?> handleInvalidBookingDate(
            InvalidBookingDateException ex) {
        ApiError apiError = new ApiError();
        apiError.setStatus(400);
        apiError.setError("Bad Request");
        apiError.setMessage(ex.getMessage());


        return ResponseEntity.badRequest().body(apiError);
    }
}
