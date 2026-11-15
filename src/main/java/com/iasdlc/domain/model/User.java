package com.iasdlc.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id, String fullName, String email, String passwordHash, Instant createdAt) {

    public static User register(String fullName, String email, String passwordHash, Instant now) {
        return new User(UUID.randomUUID(), fullName.trim(), normalizeEmail(email), passwordHash, now);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
