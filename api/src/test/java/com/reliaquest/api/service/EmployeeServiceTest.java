package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.ApiClient;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDataDTO;
import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

class EmployeeServiceTest {

    @Mock
    private ApiClient apiClient;

    @InjectMocks
    private EmployeeService employeeService;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

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

    @Test
    void getAllEmployees_returnsEmployees_whenDataExists() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        List<Employee> employees = List.of(
                employee(id1, "Alice", 1000, "Engineer", 23, "abc@gmail.com"),
                employee(id2, "Bob", 1500, "Engineer-II", 23, "abc1@gmail.com"));
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(employees);

        when(apiClient.getAllEmployees()).thenReturn(response);

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2).containsAll(employees);
        verify(apiClient).getAllEmployees();
    }

    @Test
    void getAllEmployees_throwsException_whenNoData() {
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(List.of());

        when(apiClient.getAllEmployees()).thenReturn(response);

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            employeeService.getAllEmployees();
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(ex.getMessage()).contains("No employees found");
    }

    @Test
    void getEmployeeById_returnsEmployee() {
        String id = UUID.randomUUID().toString();

        Employee emp = employee(id, "Alice", 20000, "Engineer-I", 23, "aaa@gmail.com");
        EmployeeDataDTO<Employee> response = new EmployeeDataDTO<>();
        response.setData(emp);

        when(apiClient.getEmployeeById(id)).thenReturn(response);

        Employee result = employeeService.getEmployeeById(id);

        assertThat(result).isEqualTo(emp);
        verify(apiClient).getEmployeeById(id);
    }

    @Test
    void searchEmployeesByName_returnsFilteredEmployees() {
        List<Employee> employees = List.of(
                employee("1", "David main", 1000, "Engineer", 23, "abc@gmail.com"),
                employee("2", "Alic main", 1500, "Engineer-II", 23, "abc1@gmail.com"));
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(employees);

        when(apiClient.getAllEmployees()).thenReturn(response);

        List<Employee> result = employeeService.searchEmployeesByName("main");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getName).allMatch(name -> name.toLowerCase()
                .contains("main"));
    }

    @Test
    void getHighestSalary_returnsMaxSalary() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        List<Employee> employees = List.of(
                employee(id1, "David main", 1000, "Engineer", 23, "abc@gmail.com"),
                employee(id2, "Alic main", 1500, "Engineer-II", 23, "abc1@gmail.com"));
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(employees);

        when(apiClient.getAllEmployees()).thenReturn(response);

        int maxSalary = employeeService.getHighestSalary();

        assertThat(maxSalary).isEqualTo(1500);
    }

    @Test
    void getHighestSalary_throwsException_whenNoEmployees() {
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(List.of());

        when(apiClient.getAllEmployees()).thenReturn(response);

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            employeeService.getHighestSalary();
        });

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(ex.getMessage()).contains("No employees found");
    }

    @Test
    void getTop10HighestEarningEmployeeNames_returnsTop10() {
        // 12 employees, salaries ascending
        List<Employee> employees = List.of(
                employee("1", "E1", 100, "SE-I", 25, "test1@gmail.com"),
                employee("2", "E2", 200, "SE-II", 26, "test2@gmail.com"),
                employee("3", "E3", 300, "SE-III", 27, "test3@gmail.com"),
                employee("4", "E4", 400, "SE-I", 28, "test4@gmail.com"),
                employee("5", "E5", 500, "SE-II", 29, "test5@gmail.com"),
                employee("6", "E6", 600, "SE-III", 30, "test6@gmail.com"),
                employee("7", "E7", 700, "SE-I", 31, "test7@gmail.com"),
                employee("8", "E8", 800, "SE-II", 32, "test8@gmail.com"),
                employee("9", "E9", 900, "SE-III", 33, "test9@gmail.com"),
                employee("10", "E10", 1000, "SE-I", 34, "test10@gmail.com"),
                employee("11", "E11", 1100, "SE-II", 35, "test11@gmail.com"),
                employee("12", "E12", 1200, "SE-III", 36, "test12@gmail.com"));
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(employees);

        when(apiClient.getAllEmployees()).thenReturn(response);

        List<String> top10 = employeeService.getTop10HighestEarningEmployeeNames();

        assertThat(top10).hasSize(10);
        assertThat(top10).doesNotContain("E1", "E2");
        assertThat(top10).contains("E11", "E10");

        assertThat(top10.get(0)).isEqualTo("E12");
    }

    @Test
    void createEmployee_returnsCreatedEmployee() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("New Employee");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Developer");

        Employee createdEmp = employee("10", "New Employee", 50000, "Test worker", 33, "mtmail@gmail.com");
        EmployeeDataDTO<Employee> response = new EmployeeDataDTO<>();
        response.setData(createdEmp);

        when(apiClient.createEmployee(request)).thenReturn(response);

        Employee result = employeeService.createEmployee(request);

        assertThat(result).isEqualTo(createdEmp);
        verify(apiClient).createEmployee(request);
    }

    @Test
    void deleteEmployeeById_returnsSuccessMessage_whenDeleted() {
        String id = UUID.randomUUID().toString();
        Employee emp = employee(id, "ToDelete", 1000, "Test worker", 33, "mtmail@gmail.com");
        EmployeeDataDTO<Employee> getResponse = new EmployeeDataDTO<>();
        getResponse.setData(emp);

        when(apiClient.getEmployeeById(id)).thenReturn(getResponse);
        when(apiClient.deleteEmployeeByName("ToDelete")).thenReturn(true);

        String message = employeeService.deleteEmployeeById(id);

        assertThat(message).contains("Employee 'ToDelete' deleted successfully");
        verify(apiClient).deleteEmployeeByName("ToDelete");
    }

    @Test
    void deleteEmployeeById_returnsFailureMessage_whenNameEmpty() {
        String id = UUID.randomUUID().toString();
        Employee emp = employee(id, "", 1000, "Test worker", 33, "mtmail@gmail.com");
        EmployeeDataDTO<Employee> getResponse = new EmployeeDataDTO<>();
        getResponse.setData(emp);

        when(apiClient.getEmployeeById(id)).thenReturn(getResponse);

        String message = employeeService.deleteEmployeeById(id);

        assertThat(message).isEqualTo("Employee deletion failed");
        verify(apiClient, never()).deleteEmployeeByName(anyString());
    }

    @Test
    void deleteEmployeeById_returnsFailureMessage_whenDeleteFails() {
        String id = UUID.randomUUID().toString();

        Employee emp = employee(id, "ToDelete", 1000, "Test worker", 33, "mtmail@gmail.com");
        EmployeeDataDTO<Employee> getResponse = new EmployeeDataDTO<>();
        getResponse.setData(emp);

        when(apiClient.getEmployeeById(id)).thenReturn(getResponse);
        when(apiClient.deleteEmployeeByName("ToDelete")).thenReturn(false);

        String message = employeeService.deleteEmployeeById(id);

        assertThat(message).isEqualTo("Employee deletion failed");
    }

    @Test
    void getEmployeeById_returnsNull_whenNoEmployeeFound() {
        EmployeeDataDTO<Employee> response = new EmployeeDataDTO<>();
        response.setData(null);

        when(apiClient.getEmployeeById("nonexistent")).thenReturn(response);

        Employee result = employeeService.getEmployeeById("nonexistent");

        assertThat(result).isNull();
        verify(apiClient).getEmployeeById("nonexistent");
    }

    @Test
    void getEmployeeById_throwsException_whenApiClientThrows() {
        when(apiClient.getEmployeeById(anyString()))
                .thenThrow(new EmployeeApiException("API failure", HttpStatus.INTERNAL_SERVER_ERROR));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            employeeService.getEmployeeById("anyId");
        });

        assertThat(ex.getMessage()).isEqualTo("API failure");
    }

    @Test
    void searchEmployeesByName_handlesNullEmployeeNames() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        List<Employee> employees = List.of(
                employee(id1, null, 1000, "Engineer", 23, "abc@gmail.com"),
                employee(id2, "Alic main", 1500, "Engineer-II", 23, "abc1@gmail.com"));
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(employees);

        when(apiClient.getAllEmployees()).thenReturn(response);

        List<Employee> result = employeeService.searchEmployeesByName("main");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alic main");
    }

    @Test
    void searchEmployeesByName_returnsEmptyList_whenSearchTermIsNullOrEmpty() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        List<Employee> employees = List.of(
                employee(id1, "David main", 1000, "Engineer", 23, "abc@gmail.com"),
                employee(id2, "Alic main", 1500, "Engineer-II", 23, "abc1@gmail.com"));
        EmployeeDataDTO<List<Employee>> response = new EmployeeDataDTO<>();
        response.setData(employees);

        when(apiClient.getAllEmployees()).thenReturn(response);

        List<Employee> resultNull = employeeService.searchEmployeesByName(null);
        assertThat(resultNull).isEmpty();

        List<Employee> resultEmpty = employeeService.searchEmployeesByName("");
        assertThat(resultEmpty).isEmpty();
    }

    @Test
    void deleteEmployeeById_returnsFailureMessage_whenEmployeeNotFound() {
        EmployeeDataDTO<Employee> response = new EmployeeDataDTO<>();
        response.setData(null);

        when(apiClient.getEmployeeById("1")).thenReturn(response);

        String message = employeeService.deleteEmployeeById("1");

        assertThat(message).isEqualTo("Employee deletion failed");
        verify(apiClient, never()).deleteEmployeeByName(anyString());
    }

    @Test
    void createEmployee_returnsNull_whenApiReturnsNullData() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("New Employee");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Developer");

        EmployeeDataDTO<Employee> response = new EmployeeDataDTO<>();
        response.setData(null); // API returned null

        when(apiClient.createEmployee(request)).thenReturn(response);

        Employee result = employeeService.createEmployee(request);

        assertThat(result).isNull();
    }

    @Test
    void createEmployee_throwsException_whenApiClientThrows() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("New Employee");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Developer");

        when(apiClient.createEmployee(request))
                .thenThrow(new EmployeeApiException("Create failed", HttpStatus.BAD_REQUEST));

        EmployeeApiException ex = assertThrows(EmployeeApiException.class, () -> {
            employeeService.createEmployee(request);
        });

        assertThat(ex.getMessage()).isEqualTo("Create failed");
    }
}
