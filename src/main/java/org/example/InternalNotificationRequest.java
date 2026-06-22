package org.example;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InternalNotificationRequest(
        @NotNull Long postId,
        @NotBlank String recipientUsername,
        @NotBlank String actorUsername,
        @NotBlank String sourceService,
        @NotBlank String message
) {
}
