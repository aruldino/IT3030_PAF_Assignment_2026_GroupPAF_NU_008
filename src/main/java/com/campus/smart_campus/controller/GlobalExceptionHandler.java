package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.ApiError;
import com.campus.smart_campus.exception.BusinessException;
import com.campus.smart_campus.exception.ForbiddenException;
import com.campus.smart_campus.exception.NotFoundException;
import com.campus.smart_campus.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException ex) {
        return new ApiError(LocalDateTime.now(), 404, "Not Found", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ApiError(LocalDateTime.now(), 400, "Validation Failed", "Please correct the highlighted fields.", errors);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessException(BusinessException ex) {
        return new ApiError(LocalDateTime.now(), 400, "Business Rule Violation", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorizedException(UnauthorizedException ex) {
        return new ApiError(LocalDateTime.now(), 401, "Unauthorized", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(ForbiddenException ex) {
        return new ApiError(LocalDateTime.now(), 403, "Forbidden", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableRequest(HttpMessageNotReadableException ex) {
        return new ApiError(LocalDateTime.now(), 400, "Malformed Request", "Request payload contains invalid or unsupported values.", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Cannot delete resource because it is referenced by other records (bookings, maintenance, etc.)";
        if (ex.getMessage() != null && ex.getMessage().contains("BOOKINGS")) {
            message = "Cannot delete resource: This resource has active bookings. Please cancel or modify the bookings first.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("MAINTENANCE")) {
            message = "Cannot delete resource: This resource has maintenance tickets. Please resolve or close them first.";
        }
        return new ApiError(LocalDateTime.now(), 409, "Data Integrity Violation", message, Map.of());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception ex) {
        ex.printStackTrace();
        return new ApiError(LocalDateTime.now(), 500, "Internal Server Error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred", Map.of());
    }
}
