package com.reliaquest.api.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.reliaquest.api.response.ApiResponse;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptions_returnsBadRequestWithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError("employee", "name", "must not be blank");
        FieldError error2 = new FieldError("employee", "email", "must be a valid email");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        ResponseEntity<ApiResponse<String>> response = exceptionHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("Failed");
        assertThat(response.getBody().getData()).isEqualTo("Validation failed");
        assertThat(response.getBody().getMessage()).contains("name=must not be blank");
        assertThat(response.getBody().getMessage()).contains("email=must be a valid email");
    }

    @Test
    void handleEmployeeApiException_returnsResponseEntityWithExceptionStatus() {
        EmployeeApiException ex = new EmployeeApiException("Employee not found", HttpStatus.NOT_FOUND);

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleEmployeeApiException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Employee not found");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    void handleWebClientRequestException_shouldReturnServiceUnavailable() {
        WebClientRequestException mockException = mock(WebClientRequestException.class);
        when(mockException.getMessage()).thenReturn("Connection refused");
        ResponseEntity<String> response = exceptionHandler.handleWebClientRequestException(mockException);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("Service is unavailable");
        assertThat(response.getBody()).contains("Connection refused");

    }

}
