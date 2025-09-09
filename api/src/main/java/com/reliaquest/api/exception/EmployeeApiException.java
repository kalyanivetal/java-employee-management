package com.reliaquest.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmployeeApiException extends RuntimeException {

    private final HttpStatus status;

    public EmployeeApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public EmployeeApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
