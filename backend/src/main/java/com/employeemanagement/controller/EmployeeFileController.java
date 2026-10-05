package com.employeemanagement.controller;

import com.employeemanagement.dto.ApiResponse;
import com.employeemanagement.dto.employee.EmployeePhoto;
import com.employeemanagement.dto.employee.EmployeeResponse;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.service.EmployeeService;
import com.employeemanagement.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeFileController {

    private final EmployeeService employeeService;
    private final MinioService minioService;

    @PostMapping("/{id}/photo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {

        String objectName =
                employeeService.uploadPhoto(id, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "Employee photo uploaded successfully",
                                Map.of("objectName", objectName)
                        )
                );
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<InputStreamResource> getPhoto(
            @PathVariable Long id
    ) {

        EmployeePhoto photo =
                employeeService.getPhoto(id);

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            photo.contentType()
                    );
        } catch (Exception e) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(
                        new InputStreamResource(
                                photo.inputStream()
                        )
                );
    }

    @DeleteMapping("/{id}/photo")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable Long id
    ) {

        employeeService.deletePhoto(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Employee photo deleted successfully",
                        null
                )
        );
    }

    @PostMapping("/{id}/cv")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadCv(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {

        String objectName =
                employeeService.uploadCv(id, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "Employee CV uploaded successfully",
                                Map.of("objectName", objectName)
                        )
                );
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<InputStreamResource> getCv(
            @PathVariable Long id
    ) {

        InputStream inputStream =
                employeeService.getCv(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline"
                )
                .body(
                        new InputStreamResource(inputStream)
                );
    }

    @GetMapping("/{id}/cv/download")
    public ResponseEntity<InputStreamResource> downloadCv(
            @PathVariable Long id
    ) {

        InputStream inputStream =
                employeeService.downloadCv(id);

        InputStreamResource resource =
                new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"employee-" + id + "-cv.pdf\""
                )
                .body(resource);
    }

    @DeleteMapping("/{id}/cv")
    public ResponseEntity<ApiResponse<Void>> deleteCv(
            @PathVariable Long id
    ) {

        employeeService.deleteCv(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Employee CV deleted successfully",
                        null
                )
        );
    }
}