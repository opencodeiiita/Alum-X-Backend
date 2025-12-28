package com.opencode.alumxbackend.common.exception;

import com.opencode.alumxbackend.common.exception.Errors.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice()
public class GlobalExceptionHandler{


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized Access",
                ex.getMessage(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err->err.getField() + ":" + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");


        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                message,
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidResumeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResumeException(InvalidResumeException ex){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "The Resume is Invalid",
                ex.getMessage(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResumeNotFoundException(ResumeNotFoundException  ex){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Resume not Found",
                ex.getMessage(),
                java.time.LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }



}

