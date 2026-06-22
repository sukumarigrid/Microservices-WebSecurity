package org.example;

import java.time.Instant;

public record PostResponse(
        long id,
        String author,
        String title,
        String body,
        Instant createdAt,
        Instant updatedAt,
        int likeCount
) {
}
