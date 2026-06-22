package org.example;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ServiceTokenService {

    private static final String SERVICE_AUTHORITY = "ROLE_SERVICE_NOTIFICATION";

    private final JwtService jwtService;

    public ServiceTokenService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Authentication createServiceAuthentication(String serviceName) {
        String token = jwtService.issueServiceToken(serviceName, Set.of(SERVICE_AUTHORITY));
        return jwtService.authenticate(token);
    }
}
