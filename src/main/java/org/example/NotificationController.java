package org.example;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<NotificationResponse> listNotifications(Authentication authentication) {
        return notificationService.listVisibleNotifications(authentication);
    }

    @PostMapping("/internal/notifications")
    @PreAuthorize("hasRole('SERVICE_NOTIFICATION')")
    public NotificationResponse createInternalNotification(@Valid @RequestBody InternalNotificationRequest request,
                                                           Authentication authentication) {
        return notificationService.recordInternalNotification(request, authentication);
    }
}
