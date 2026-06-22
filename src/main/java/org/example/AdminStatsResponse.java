package org.example;

import java.util.List;

public record AdminStatsResponse(
        long userCount,
        long postCount,
        long notificationCount,
        List<String> usernames
) {
}
