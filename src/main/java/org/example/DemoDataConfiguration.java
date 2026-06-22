package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DemoDataConfiguration {

    @Bean
    CommandLineRunner seedDemoData(UserAccountRepository userAccountRepository,
                                   PostRepository postRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userAccountRepository.exists("admin")) {
                userAccountRepository.save(new UserAccount(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        Set.of("ROLE_ADMIN")));
            }

            if (!userAccountRepository.exists("alice")) {
                userAccountRepository.save(new UserAccount(
                        "alice",
                        passwordEncoder.encode("alice123"),
                        Set.of("ROLE_USER")));
            }

            if (!userAccountRepository.exists("bob")) {
                userAccountRepository.save(new UserAccount(
                        "bob",
                        passwordEncoder.encode("bob123"),
                        Set.of("ROLE_USER")));
            }

            if (postRepository.count() == 0) {
                postRepository.create("alice", "Welcome to the secured API",
                        "This post is owned by Alice and can only be edited by her or an admin.");
                postRepository.create("bob", "Service-to-service access",
                        "This post demonstrates that likes create notifications through an internal service identity.");
            }
        };
    }
}
