package com.employeemanagement.dto.notification;

import com.employeemanagement.entity.NotificationAction;
import com.employeemanagement.entity.NotificationEntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private Long actorId;

    private String actorName;

    private NotificationAction action;

    private NotificationEntityType entityType;

    private Long entityId;

    private String message;

    private boolean read;

    private LocalDateTime createdAt;

    private String details;
}