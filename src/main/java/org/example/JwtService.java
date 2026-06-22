package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueUserToken(UserAccount account) {
        return issueToken(account.username(), account.authorities(), jwtProperties.accessTokenTtl(), "user");
    }

    public String issueServiceToken(String serviceName, Collection<String> authorities) {
        return issueToken(serviceName, authorities, jwtProperties.serviceTokenTtl(), "service");
    }

    public Duration accessTokenTtl() {
        return jwtProperties.accessTokenTtl();
    }

    public Authentication authenticate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities = readAuthorities(claims);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
        authentication.setDetails(Map.of(
                "tokenUse", claims.get("tokenUse", String.class),
                "issuer", claims.getIssuer()
        ));
        return authentication;
    }

    private String issueToken(String subject, Collection<String> authorities, Duration ttl, String tokenUse) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(jwtProperties.issuer())
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl)))
                .claim("authorities", new ArrayList<>(authorities))
                .claim("tokenUse", tokenUse)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Collection<? extends GrantedAuthority> readAuthorities(Claims claims) {
        Object rawAuthorities = claims.get("authorities");
        if (!(rawAuthorities instanceof Collection<?> collection)) {
            return List.of();
        }

        return collection.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
