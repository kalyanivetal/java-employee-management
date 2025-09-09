package com.reliaquest.api.dto;

import lombok.Data;

@Data
public class EmployeeDataDTO<T> {
    private T data;
    private String status;
}
