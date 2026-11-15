package com.iasdlc.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Versión de un agente. {@code instructions} es el texto del agent.md de esa versión. */
public record AgentVersion(UUID id, String version, String commitSha, String instructions,
                           String changeNotes, Instant createdAt) {
}
