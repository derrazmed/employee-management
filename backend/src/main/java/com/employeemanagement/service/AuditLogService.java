package com.employeemanagement.service;

import com.employeemanagement.entity.AuditAction;
import com.employeemanagement.entity.AuditEntityType;
import com.employeemanagement.entity.AuditLog;
import com.employeemanagement.entity.User;
import com.employeemanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            User actor,
            AuditAction action,
            AuditEntityType entityType,
            Long entityId,
            String description,
            String details,
            String ipAddress
    ) {
        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .details(details)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}