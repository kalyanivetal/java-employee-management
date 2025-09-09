package com.reliaquest.api;

import static org.assertj.core.api.Assertions.*;

import com.reliaquest.api.exception.EmployeeApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class EmployeeApiExceptionTest {

    @Test
    void constructor_withMessageAndStatus_setsFieldsCorrectly() {
        String message = "Employee not found";
        HttpStatus status = HttpStatus.NOT_FOUND;

        EmployeeApiException ex = new EmployeeApiException(message, status);

        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getStatus()).isEqualTo(status);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructor_withMessageStatusAndCause_setsFieldsCorrectly() {
        String message = "Connection error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Throwable cause = new RuntimeException("Connection failed");

        EmployeeApiException ex = new EmployeeApiException(message, status, cause);

        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getStatus()).isEqualTo(status);
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
