package com.employeemanagement.dto.employee;

import java.io.InputStream;

public record EmployeePhoto(
        InputStream inputStream,
        String contentType
) {
}
