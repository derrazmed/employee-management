package com.employeemanagement.config;

import com.employeemanagement.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioInitializer implements CommandLineRunner {

    private final MinioService minioService;

    @Override
    public void run(String... args) {

        minioService.initializeBucket();
    }
}