package com.reliaquest.api.service;

import com.reliaquest.api.client.ApiClient;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDataDTO;
import com.reliaquest.api.exception.EmployeeApiException;
import com.reliaquest.api.model.Employee;
import io.netty.util.internal.StringUtil;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeService {

    private final ApiClient apiClient;

    public EmployeeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees from API client");

        EmployeeDataDTO<List<Employee>> employees = apiClient.getAllEmployees();

        if (employees == null
                || employees.getData() == null
                || employees.getData().isEmpty()) {
            log.warn("No employees found");
            throw new EmployeeApiException("No employees found", HttpStatus.NO_CONTENT);
        } else {
            log.info("Fetched {} employees", employees.getData().size());
        }

        return employees.getData();
    }

    public Employee getEmployeeById(String id) {
        return (Employee) apiClient.getEmployeeById(id).getData();
    }

    public List<Employee> searchEmployeesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return this.getAllEmployees().stream()
                .filter(emp ->
                        emp.getName() != null && emp.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public int getHighestSalary() {
        return this.getAllEmployees().stream()
                .mapToInt(Employee::getSalary)
                .max()
                .getAsInt();
    }

    public List<String> getTop10HighestEarningEmployeeNames() {
        return this.getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(EmployeeCreateRequest request) {
        return apiClient.createEmployee(request).getData();
    }

    public String deleteEmployeeById(String id) {
        Employee employeeById = this.getEmployeeById(id);
        if (employeeById != null) {
            String name = employeeById.getName();
            if (!StringUtil.isNullOrEmpty(name)) {
                log.info("Deleting Employee with name: {}", name);
                boolean status = apiClient.deleteEmployeeByName(name);
                if (status) {
                    return String.format("Employee '%s' deleted successfully", name);
                }
            }
        }
        return "Employee deletion failed";
    }
}
