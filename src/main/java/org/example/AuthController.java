package org.example;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final JwtService jwtService;

    public AuthController(UserAccountService userAccountService, JwtService jwtService) {
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
    }

    @PostMapping({"/token", "/login"})
    public TokenResponse issueToken(@Valid @RequestBody LoginRequest request) {
        UserAccount account = userAccountService.authenticate(request.username(), request.password());
        return new TokenResponse(
                jwtService.issueUserToken(account),
                "Bearer",
                jwtService.accessTokenTtl().toSeconds(),
                account.username(),
                account.authorities());
    }
}
