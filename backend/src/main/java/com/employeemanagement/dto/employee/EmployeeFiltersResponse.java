package com.employeemanagement.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmployeeFiltersResponse {

    private List<String> departments;
    private List<String> jobTitles;
}
