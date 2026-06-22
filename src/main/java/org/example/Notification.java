package org.example;

import java.time.Instant;

public record Notification(
        long id,
        long postId,
        String recipientUsername,
        String actorUsername,
        String sourceService,
        String message,
        Instant createdAt
) {
}
