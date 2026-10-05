package com.employeemanagement.service;

import com.employeemanagement.dto.notification.NotificationResponse;
import com.employeemanagement.entity.Notification;
import com.employeemanagement.entity.NotificationAction;
import com.employeemanagement.entity.NotificationEntityType;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.UserType;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.repository.NotificationRepository;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public void notifySuperAdmins(
            NotificationAction action,
            NotificationEntityType entityType,
            Long entityId,
            String message,
            String details
    ) {
        User actor = getCurrentUser();

        List<User> superAdmins =
                userRepository.findByUserType(UserType.SUPER_ADMIN);

        List<Notification> notifications = superAdmins.stream()
                .map(superAdmin -> Notification.builder()
                        .recipient(superAdmin)
                        .actor(actor)
                        .action(action)
                        .entityType(entityType)
                        .entityId(entityId)
                        .message(message)
                        .details(details)
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        List<Notification> savedNotifications =
                notificationRepository.saveAll(notifications);

        for (Notification notification : savedNotifications) {

            NotificationResponse notificationResponse =
                    toResponse(notification);

            messagingTemplate.convertAndSendToUser(
                    notification.getRecipient().getEmail(),
                    "/queue/notifications",
                    notificationResponse
            );
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .findByRecipientOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .findByRecipientAndReadFalseOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get the unread notification count.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {

        User currentUser = getCurrentUser();

        return notificationRepository
                .countByRecipientAndReadFalse(currentUser);
    }

    @Transactional
    public void markAsRead(Long notificationId) {

        User currentUser = getCurrentUser();

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found"
                        )
                );

        // Make sure a Super Admin cannot modify another
        // Super Admin's notification.
        if (!notification.getRecipient().getId()
                .equals(currentUser.getId())) {

            throw new ResourceNotFoundException(
                    "Notification not found"
            );
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications belonging to the current
     * Super Admin as read.
     */
    @Transactional
    public void markAllAsRead() {

        User currentUser = getCurrentUser();

        List<Notification> notifications =
                notificationRepository
                        .findByRecipientAndReadFalseOrderByCreatedAtDesc(
                                currentUser
                        );

        notifications.forEach(notification ->
                notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }

    /**
     * Get the currently authenticated user from Spring Security.
     */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {
        return new NotificationResponse(
                notification.getId(),
                notification.getActor().getId(),
                notification.getActor().getName(),
                notification.getAction(),
                notification.getEntityType(),
                notification.getEntityId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getDetails()
        );
    }

    @Transactional
    public void deleteNotification(Long id) {
        User currentUser = getCurrentUser();

        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found")
                );

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You cannot delete this notification"
            );
        }

        notificationRepository.delete(notification);
    }
}