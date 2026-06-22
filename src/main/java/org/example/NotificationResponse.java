package org.example;

import java.time.Instant;

public record NotificationResponse(
        long id,
        long postId,
        String recipientUsername,
        String actorUsername,
        String sourceService,
        String message,
        Instant createdAt
) {
}
