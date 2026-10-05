package com.employeemanagement.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String jobTitle;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String photoObjectName;
    private String cvObjectName;
}