package org.example;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank String title,
        @NotBlank String body
) {
}
