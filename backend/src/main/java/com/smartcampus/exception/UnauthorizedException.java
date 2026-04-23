package com.smartcampus.exception;

/** Thrown when a user lacks permission to perform an action — maps to HTTP 403. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
