package com.reliaquest.api.client;

import static com.reliaquest.api.server.TraceIdFilter.MDC_TRACE_ID_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDataDTO;
import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.response.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String EMPLOYEE_ENDPOINT = "/api/v1/employee";
    private static final String HEADER_X_TRACE_ID = "X-Trace-Id";

    public ApiClient(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public EmployeeDataDTO<List<Employee>> getAllEmployees() {
        log.info("Fetching all employees from mock server");
        try {
            String response = webClient
                    .get()
                    .uri(EMPLOYEE_ENDPOINT)
                    .header(HEADER_X_TRACE_ID, MDC.get(MDC_TRACE_ID_KEY))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<EmployeeDataDTO<List<Employee>>>() {});

        } catch (WebClientResponseException ex) {
            log.error("Server responded with error :{}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            throw new EmployeeApiException("Error from mock server: " + ex.getMessage(), status);
        } catch (WebClientRequestException ex) {
            log.error("Cannot connect to mock server: {}", ex);
            throw new EmployeeApiException("Unable to connect to mock server", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (JsonProcessingException ex) {
            log.error("Error parsing JSON from mock server", ex);
            throw new EmployeeApiException("Error parsing JSON from mock server", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error("Unexpected error", ex);
            throw new EmployeeApiException("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public EmployeeDataDTO<Employee> getEmployeeById(String id) {
        log.info("Fetching employee with id: {} from mock server", id);
        try {
            String response = webClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder.path(EMPLOYEE_ENDPOINT + "/{id}").build(id))
                    .header(HEADER_X_TRACE_ID, MDC.get(MDC_TRACE_ID_KEY))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<EmployeeDataDTO<Employee>>() {});
        } catch (WebClientResponseException ex) {
            log.error("Server responded with error :{}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            } else if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) {
                throw new EmployeeApiException("Unable to connect to mock server", status);
            } else if (status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                throw new EmployeeApiException("Too many requests", status);
            }
            throw new EmployeeApiException("Internal Server Error", status);
        } catch (WebClientRequestException ex) {
            log.error("Cannot connect to mock server: {}", ex.getMessage());
            throw new EmployeeApiException("Unable to connect to mock server", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception ex) {
            log.error("Unexpected error", ex);
            throw new EmployeeApiException("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean deleteEmployeeByName(String name) {
        log.info("Deleting employee data with name: {}", name);
        try {
            String response = webClient
                    .method(HttpMethod.DELETE)
                    .uri(EMPLOYEE_ENDPOINT)
                    .header(HEADER_X_TRACE_ID, MDC.get(MDC_TRACE_ID_KEY))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", name))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Server response: {}", response);

            ApiResponse<Boolean> result =
                    objectMapper.readValue(response, new TypeReference<ApiResponse<Boolean>>() {});
            if (Boolean.TRUE.equals(result.getData())) {
                log.info("Employee {} deleted successfully", name);
                return true;
            } else {
                throw new EmployeeApiException("Failed to delete employee" + name, HttpStatus.BAD_REQUEST);
            }

        } catch (WebClientResponseException ex) {
            log.error("Server responded with error :{}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            throw new EmployeeApiException("Error deleting employee: " + name, status, ex);
        } catch (Exception ex) {
            log.error("Unexpected error during delete", ex);
            throw new EmployeeApiException("Failed to delete employee", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public EmployeeDataDTO<Employee> createEmployee(EmployeeCreateRequest request) {
        try {
            String response = webClient
                    .post()
                    .uri(EMPLOYEE_ENDPOINT)
                    .header(HEADER_X_TRACE_ID, MDC.get(MDC_TRACE_ID_KEY))
                    .body(Mono.just(request), EmployeeCreateRequest.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(response, new TypeReference<EmployeeDataDTO<Employee>>() {});
        } catch (WebClientResponseException ex) {
            log.error("Server responded with error :{}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            throw new EmployeeApiException("Error creating employee", status, ex);
        } catch (Exception ex) {
            log.error("Error creating employee", ex);
            throw new EmployeeApiException("Unable to create employee", HttpStatus.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
