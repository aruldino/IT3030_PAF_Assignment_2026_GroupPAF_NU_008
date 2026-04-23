package com.smartcampus.exception;

/** Thrown when a request contains invalid or missing data — maps to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
