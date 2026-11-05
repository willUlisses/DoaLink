package com.doalink_api.exception;

import com.doalink_api.dtos.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

}
