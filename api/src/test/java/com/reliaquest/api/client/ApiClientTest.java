package com.reliaquest.api.client;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDataDTO;
import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.response.ApiResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
@SpringBootTest
public class ApiClientTest {

    private MockWebServer mockWebServer;
    private ApiClient apiClient;
    private ObjectMapper objectMapper = new ObjectMapper();
    private WebClient webClient;

    private Employee employee(String id, String name, int salary, String title, int age, String email) {
        Employee emp = new Employee();
        emp.setId(id);
        emp.setName(name);
        emp.setSalary(salary);
        emp.setTitle(title);
        emp.setAge(age);
        emp.setEmail(email);
        return emp;
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        apiClient = new ApiClient(webClient, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getAllEmployees_returnsEmployeesList() throws Exception {
        List<Employee> employees = List.of(
                employee(UUID.randomUUID().toString(), "Alice", 1000, "Worker", 35, "test1@gmail.com"),
                employee(UUID.randomUUID().toString(), "Bob", 1500, "Worker", 35, "test2@gmail.com"));

        EmployeeDataDTO<List<Employee>> dto = new EmployeeDataDTO<>();
        dto.setData(employees);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(toJson(dto))
                .addHeader("Content-Type", "application/json"));

        List<Employee> result = apiClient.getAllEmployees().getData();
        assertThat(result).hasSize(2).extracting(Employee::getName).contains("Alice", "Bob");
    }

    @Test
    void getEmployeeById_returnsEmployee() throws Exception {
        String id = UUID.randomUUID().toString();
        Employee employee = employee(id, "Alice", 1000, "Worker", 35, "test@gmail.com");

        EmployeeDataDTO<Employee> dto = new EmployeeDataDTO<>();
        dto.setData(employee);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(toJson(dto))
                .addHeader("Content-Type", "application/json"));

        Employee result = apiClient.getEmployeeById(id).getData();
        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void createEmployee_returnsCreatedEmployee() throws Exception {
        String id = UUID.randomUUID().toString();
        Employee employee = employee(id, "New Emp", 5000, "Worker", 35, "test@gmail.com");

        EmployeeDataDTO<Employee> dto = new EmployeeDataDTO<>();
        dto.setData(employee);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody(toJson(dto))
                .addHeader("Content-Type", "application/json"));

        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setName("New Emp");
        req.setSalary(5000);
        req.setAge(25);
        req.setTitle("Engineer");

        Employee result = apiClient.createEmployee(req).getData();
        assertThat(result.getName()).isEqualTo("New Emp");
    }

    @Test
    void deleteEmployeeByName_successfulDelete() throws Exception {
        ApiResponse<Boolean> apiResponse = ApiResponse.ok(true);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(toJson(apiResponse))
                .addHeader("Content-Type", "application/json"));

        boolean result = apiClient.deleteEmployeeByName("Alice");
        assertThat(result).isTrue();
    }

    @Test
    void getAllEmployees_handlesHttp4xxError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("Bad Request"));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.getAllEmployees();
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("400");
    }

    @Test
    void getEmployeeById_handlesNotFoundError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404).setBody("Not Found"));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.getEmployeeById(UUID.randomUUID().toString());
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Unable to connect to mock server");
    }

    @Test
    void getEmployeeById_handlesTooManyRequests() {
        String id = UUID.randomUUID().toString();

        mockWebServer.enqueue(new MockResponse().setResponseCode(429).setBody("Too Many Requests"));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.getEmployeeById(id);
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(ex.getMessage()).contains("Too many requests");
    }

    @Test
    void createEmployee_handlesServerError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setName("Fail Emp");

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.createEmployee(req);
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteEmployeeByName_handlesDeleteFailure() throws Exception {
        ApiResponse<Boolean> apiResponse = ApiResponse.ok(false);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(toJson(apiResponse))
                .addHeader("Content-Type", "application/json"));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.deleteEmployeeByName("NonExistent");
        });

        assertThat(ex.getMessage()).contains("Failed to delete employee");
    }

    @Test
    void getAllEmployees_handlesJsonProcessingException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("INVALID_JSON"));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.getAllEmployees();
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ex.getMessage()).contains("parsing JSON");
    }

    @Test
    void getAllEmployees_handlesNetworkException() {
        try {
            mockWebServer.shutdown(); // simulate network error
        } catch (Exception ignored) {
        }

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.getAllEmployees();
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(ex.getMessage()).contains("Unable to connect");
    }

    @Test
    void deleteEmployeeByName_handlesHttpError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            apiClient.deleteEmployeeByName("Alice");
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
