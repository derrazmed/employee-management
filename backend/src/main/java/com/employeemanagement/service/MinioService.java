package com.employeemanagement.service;

import com.employeemanagement.exception.FileStorageException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public void initializeBucket() {

        try {

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not initialize MinIO bucket",
                    e
            );
        }
    }

    public String uploadFile(
            String objectName,
            MultipartFile file
    ) {

        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(
                                    inputStream,
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            return objectName;

        } catch (Exception e) {

            throw new FileStorageException(
                    "Could not upload file",
                    e
            );
        }
    }

    public InputStream downloadFile(String objectName) {

        try {

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not retrieve file",
                    e
            );
        }
    }

    public void deleteFile(String objectName) {

        try {

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not delete file",
                    e
            );
        }
    }

    public String getContentType(String objectName) {

        try {

            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            ).contentType();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not retrieve file information",
                    e
            );
        }
    }
}