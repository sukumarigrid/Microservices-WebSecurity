package org.example;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final String SERVICE_AUTHORITY = "ROLE_SERVICE_NOTIFICATION";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse recordInternalNotification(InternalNotificationRequest request, Authentication authentication) {
        requireServiceIdentity(authentication);
        return toResponse(notificationRepository.save(
                request.postId(),
                request.recipientUsername(),
                request.actorUsername(),
                request.sourceService(),
                request.message()));
    }

    public List<NotificationResponse> listVisibleNotifications(Authentication authentication) {
        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return notificationRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }

        return notificationRepository.findForRecipient(authentication.getName()).stream()
                .map(this::toResponse)
                .toList();
    }

    public long count() {
        return notificationRepository.count();
    }

    private void requireServiceIdentity(Authentication authentication) {
        if (!hasAuthority(authentication, SERVICE_AUTHORITY)) {
            throw new AccessDeniedException("Only the internal notification service can create notifications");
        }
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(item -> item.getAuthority().equals(authority));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.postId(),
                notification.recipientUsername(),
                notification.actorUsername(),
                notification.sourceService(),
                notification.message(),
                notification.createdAt());
    }
}
