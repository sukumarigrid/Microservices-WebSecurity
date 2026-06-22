package org.example;

import java.util.Set;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        Set<String> authorities
) {
}
