package com.reliaquest.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {
    @NotBlank
    private String name;

    @Min(1)
    private int salary;

    @Min(16)
    @Max(75)
    private int age;

    @NotBlank
    private String title;
}
