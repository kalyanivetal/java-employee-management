package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.response.ApiResponse;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<Employee, EmployeeCreateRequest> {

    private final EmployeeService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        ApiResponse<List<Employee>> apiResponse = ApiResponse.ok(service.getAllEmployees());
        return ResponseEntity.ok(apiResponse.getData());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        ApiResponse<List<Employee>> apiResponse = ApiResponse.ok(service.searchEmployeesByName(searchString));
        return ResponseEntity.ok(apiResponse.getData());
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        ApiResponse<Employee> apiResponse = ApiResponse.ok(service.getEmployeeById(id));
        return ResponseEntity.ok(apiResponse.getData());
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        ApiResponse<Integer> apiResponse = ApiResponse.ok(service.getHighestSalary());
        return ResponseEntity.ok(apiResponse.getData());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        ApiResponse<List<String>> apiResponse = ApiResponse.ok(service.getTop10HighestEarningEmployeeNames());
        return ResponseEntity.ok(apiResponse.getData());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody @Valid EmployeeCreateRequest employeeInput) {
        log.info("Request: {}", employeeInput);
        ApiResponse<Employee> apiResponse = ApiResponse.ok(service.createEmployee(employeeInput));
        return ResponseEntity.ok(apiResponse.getData());
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        ApiResponse<String> apiResponse = ApiResponse.ok(service.deleteEmployeeById(id));
        return ResponseEntity.ok(apiResponse.getData());
    }
}
