package com.employeemanagement.service;

import com.employeemanagement.dto.employee.EmployeePhoto;
import com.employeemanagement.dto.employee.EmployeeRequest;
import com.employeemanagement.dto.employee.EmployeeResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.NotificationAction;
import com.employeemanagement.entity.NotificationEntityType;
import com.employeemanagement.entity.Permission;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PermissionService permissionService;

    private final MinioService minioService;
    private final FileValidationService fileValidationService;

    private final NotificationService notificationService;

    public EmployeeResponse createEmployee(EmployeeRequest request) {

        permissionService.requirePermission(
                com.employeemanagement.entity.Permission.CREATE
        );

        Employee employee = new Employee();

        mapRequestToEntity(request, employee);

        Employee savedEmployee = employeeRepository.save(employee);

        String details = String.format(
                "Employee created with name '%s', email '%s', position '%s', and department '%s'.",
                savedEmployee.getFirstName() + " " + savedEmployee.getLastName(),
                savedEmployee.getEmail(),
                savedEmployee.getJobTitle(),
                savedEmployee.getDepartment()
        );


        notificationService.notifySuperAdmins(
                NotificationAction.CREATE,
                NotificationEntityType.EMPLOYEE,
                savedEmployee.getId(),
                "created employee " + savedEmployee.getFirstName() + " " + savedEmployee.getLastName(),
                details
        );

        return mapToResponse(savedEmployee);
    }

    public Page<EmployeeResponse> getEmployees(
            String search,
            Pageable pageable
    ) {

        permissionService.requirePermission(
                com.employeemanagement.entity.Permission.READ
        );

        Page<Employee> employees;

        if (search == null || search.trim().isEmpty()) {

            employees = employeeRepository.findAll(pageable);

        } else {

            employees =
                    employeeRepository
                            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                    search,
                                    search,
                                    search,
                                    pageable
                            );
        }

        return employees.map(this::mapToResponse);
    }

    public EmployeeResponse getEmployee(Long id) {

        permissionService.requirePermission(
                com.employeemanagement.entity.Permission.READ
        );

        Employee employee = findEmployee(id);

        return mapToResponse(employee);
    }

    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request
    ) {

        permissionService.requirePermission(
                com.employeemanagement.entity.Permission.UPDATE
        );

        Employee employee = findEmployee(id);

        mapRequestToEntity(request, employee);

        Employee updatedEmployee = employeeRepository.save(employee);

        String details = String.format(
                "Employee updated with name '%s', email '%s', position '%s', and department '%s'.",
                updatedEmployee.getFirstName() + " " + updatedEmployee.getLastName(),
                updatedEmployee.getEmail(),
                updatedEmployee.getJobTitle(),
                updatedEmployee.getDepartment()
        );

        notificationService.notifySuperAdmins(
                NotificationAction.UPDATE,
                NotificationEntityType.EMPLOYEE,
                updatedEmployee.getId(),
                "updated employee " + updatedEmployee.getFirstName() + " " + updatedEmployee.getLastName(),
                details
        );

        return mapToResponse(updatedEmployee);
    }

    public void deleteEmployee(Long id) {

        permissionService.requirePermission(
                com.employeemanagement.entity.Permission.DELETE
        );

        Employee employee = findEmployee(id);
        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        Long employeeId = employee.getId();

        String details = String.format(
                "Employee with id '%d' deleted with name '%s', email '%s', position '%s', and department '%s'.",
                employeeId,
                employeeName,
                employee.getEmail(),
                employee.getJobTitle(),
                employee.getDepartment()
        );

        employeeRepository.delete(employee);

        notificationService.notifySuperAdmins(
                NotificationAction.DELETE,
                NotificationEntityType.EMPLOYEE,
                employeeId,
                "deleted employee " + employeeName,
                details
        );
    }

    private Employee findEmployee(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        )
                );
    }

    private void mapRequestToEntity(
            EmployeeRequest request,
            Employee employee
    ) {

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setJobTitle(request.getJobTitle());
        employee.setDepartment(request.getDepartment());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
    }

    private EmployeeResponse mapToResponse(
            Employee employee
    ) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getJobTitle(),
                employee.getDepartment(),
                employee.getHireDate(),
                employee.getSalary(),
                employee.getPhotoObjectName(),
                employee.getCvObjectName()
        );
    }

    public String uploadPhoto(
            Long employeeId,
            MultipartFile file
    ) {

        permissionService.requirePermission(Permission.UPDATE);

        fileValidationService.validatePhoto(file);

        Employee employee = findEmployee(employeeId);

        if (employee.getPhotoObjectName() != null) {

            minioService.deleteFile(
                    employee.getPhotoObjectName()
            );
        }

        String objectName =
                "employees/" +
                        employeeId +
                        "/photo/" +
                        java.util.UUID.randomUUID() +
                        "-" +
                        file.getOriginalFilename();

        minioService.uploadFile(
                objectName,
                file
        );

        employee.setPhotoObjectName(objectName);

        employeeRepository.save(employee);

        return objectName;
    }

    public EmployeePhoto getPhoto(Long employeeId) {

        permissionService.requirePermission(Permission.READ);

        Employee employee = findEmployee(employeeId);

        if (employee.getPhotoObjectName() == null) {
            throw new ResourceNotFoundException(
                    "Employee does not have a photo"
            );
        }

        String objectName = employee.getPhotoObjectName();

        InputStream inputStream =
                minioService.downloadFile(objectName);

        String contentType =
                minioService.getContentType(objectName);

        return new EmployeePhoto(
                inputStream,
                contentType
        );
    }


    public void deletePhoto(Long employeeId) {

        permissionService.requirePermission(Permission.UPDATE);

        Employee employee = findEmployee(employeeId);

        if (employee.getPhotoObjectName() == null) {

            throw new ResourceNotFoundException(
                    "Employee does not have a photo"
            );
        }

        minioService.deleteFile(
                employee.getPhotoObjectName()
        );

        employee.setPhotoObjectName(null);

        employeeRepository.save(employee);
    }

    public String uploadCv(
            Long employeeId,
            MultipartFile file
    ) {

        permissionService.requirePermission(Permission.UPDATE);

        fileValidationService.validateCv(file);

        Employee employee = findEmployee(employeeId);

        // Delete previous CV if one exists
        if (employee.getCvObjectName() != null) {

            minioService.deleteFile(
                    employee.getCvObjectName()
            );
        }

        String objectName =
                "employees/" +
                        employeeId +
                        "/cv/" +
                        java.util.UUID.randomUUID() +
                        ".pdf";

        minioService.uploadFile(
                objectName,
                file
        );

        employee.setCvObjectName(objectName);

        employeeRepository.save(employee);

        return objectName;
    }

    public InputStream getCv(Long employeeId) {

        permissionService.requirePermission(Permission.READ);

        Employee employee = findEmployee(employeeId);

        if (employee.getCvObjectName() == null) {
            throw new ResourceNotFoundException(
                    "Employee does not have a CV"
            );
        }

        return minioService.downloadFile(
                employee.getCvObjectName()
        );
    }

    public void deleteCv(Long employeeId) {

        permissionService.requirePermission(Permission.UPDATE);

        Employee employee = findEmployee(employeeId);

        if (employee.getCvObjectName() == null) {

            throw new ResourceNotFoundException(
                    "Employee does not have a CV"
            );
        }

        minioService.deleteFile(
                employee.getCvObjectName()
        );

        employee.setCvObjectName(null);

        employeeRepository.save(employee);
    }

    public InputStream downloadCv(Long employeeId) {

        permissionService.requirePermission(Permission.READ);

        Employee employee = findEmployee(employeeId);

        if (employee.getCvObjectName() == null) {

            throw new ResourceNotFoundException(
                    "Employee does not have a CV"
            );
        }

        return minioService.downloadFile(
                employee.getCvObjectName()
        );
    }
}