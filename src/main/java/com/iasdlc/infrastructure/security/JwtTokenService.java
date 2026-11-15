package com.iasdlc.infrastructure.security;

import com.iasdlc.application.port.out.TokenIssuer;
import com.iasdlc.domain.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Emite y valida los JWT (HS256). El "subject" del token es el id del usuario.
 */
@Component
public class JwtTokenService implements TokenIssuer {

    private final SecretKey key;
    private final Duration expiration;
    private final Clock clock;

    public JwtTokenService(@Value("${iasdlc.security.jwt.secret}") String secret,
                           @Value("${iasdlc.security.jwt.expiration-minutes:480}") long expirationMinutes,
                           Clock clock) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "iasdlc.security.jwt.secret debe tener al menos 32 bytes para firmar con HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expiration = Duration.ofMinutes(expirationMinutes);
        this.clock = clock;
    }

    @Override
    public String issue(User user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /** Devuelve el id del usuario si el token es válido y no ha expirado. */
    public Optional<UUID> parseUserId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(UUID.fromString(subject));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
