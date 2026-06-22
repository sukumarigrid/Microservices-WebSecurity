package org.example;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "unauthorized", "Authentication is required."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "forbidden", "Access is denied.")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/token", "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/internal/**").hasRole("SERVICE_NOTIFICATION")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/posts/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserAccountRepository userAccountRepository) {
        return username -> userAccountRepository.findByUsername(username)
                .map(account -> User.withUsername(account.username())
                        .password(account.passwordHash())
                        .authorities(account.authorities().toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private static void writeJsonError(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().printf("{\"error\":\"%s\",\"message\":\"%s\"}", error, message);
    }
}
