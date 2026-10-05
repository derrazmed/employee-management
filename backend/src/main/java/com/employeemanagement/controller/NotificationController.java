package com.employeemanagement.controller;

import com.employeemanagement.dto.ApiResponse;
import com.employeemanagement.dto.notification.NotificationResponse;
import com.employeemanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
    getNotifications() {

        List<NotificationResponse> notifications =
                notificationService.getNotifications();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Notifications retrieved successfully",
                        notifications
                )
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
    getUnreadNotifications() {

        List<NotificationResponse> notifications =
                notificationService.getUnreadNotifications();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Unread notifications retrieved successfully",
                        notifications
                )
        );
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>>
    getUnreadCount() {

        long count =
                notificationService.getUnreadCount();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Unread notification count retrieved successfully",
                        count
                )
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>>
    markAsRead(@PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Notification marked as read",
                        null
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>>
    markAllAsRead() {

        notificationService.markAllAsRead();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "All notifications marked as read",
                        null
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(id);

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Notification deleted successfully", null)
        );
    }
}