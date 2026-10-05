package com.employeemanagement.service;

import com.employeemanagement.exception.FileStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class FileValidationService {

    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024;
    private static final long MAX_CV_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public void validatePhoto(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException(
                    "Photo file is required"
            );
        }

        if (file.getSize() > MAX_PHOTO_SIZE) {
            throw new FileStorageException(
                    "Photo must not exceed 5 MB"
            );
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new FileStorageException(
                    "Only JPEG, PNG and WebP images are allowed"
            );
        }
    }

    public void validateCv(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileStorageException(
                    "CV file is required"
            );
        }

        if (file.getSize() > MAX_CV_SIZE) {
            throw new FileStorageException(
                    "CV must not exceed 10 MB"
            );
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new FileStorageException(
                    "Only PDF files are allowed for the CV"
            );
        }
    }
}