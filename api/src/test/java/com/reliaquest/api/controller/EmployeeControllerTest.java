package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee1;
    private Employee employee2;
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        employee1 = employee(id1, "Alice", 1000, "Engineer", 23, "abc@gmail.com");
        employee2 = employee(id2, "Bob", 1500, "Engineer-II", 23, "abc1@gmail.com");
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
    void getAllEmployees_shouldReturnList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee1, employee2));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("Alice"))
                .andExpect(jsonPath("$[1].employee_name").value("Bob"));
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnMatching() throws Exception {
        when(employeeService.searchEmployeesByName("Ali")).thenReturn(List.of(employee1));

        mockMvc.perform(get("/api/employees/search/Ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value("Alice"));
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(id1)).thenReturn(employee1);

        mockMvc.perform(get("/api/employees/" + id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Alice"));
    }

    @Test
    void getHighestSalary_shouldReturnInt() throws Exception {
        when(employeeService.getHighestSalary()).thenReturn(150000);

        mockMvc.perform(get("/api/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("150000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnList() throws Exception {
        when(employeeService.getTop10HighestEarningEmployeeNames())
                .thenReturn(List.of("Alice", "Bob", "Carol"));

        mockMvc.perform(get("/api/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Alice"));
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setName("New Emp");
        req.setSalary(5000);
        req.setAge(25);
        req.setTitle("Engineer");
        req.setEmail("abc");

        Employee created = employee(UUID.randomUUID().toString(), "Charlie", 1500, "Engineer-II", 23, "abc1@gmail.com");

        when(employeeService.createEmployee(any())).thenReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name").value("Charlie"));
    }

    @Test
    void deleteEmployee_shouldReturnSuccessMessage() throws Exception {
        when(employeeService.deleteEmployeeById(id1)).thenReturn("Deleted");

        mockMvc.perform(delete("/api/employees/" + id1))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }
}
