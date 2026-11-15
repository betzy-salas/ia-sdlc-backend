package com.iasdlc.domain.model;

import java.util.UUID;

/** Integrante de un squad con un rol; puede ser titular o suplente de ese rol. */
public record SquadMember(UUID id, UUID userId, SquadRole role, boolean substitute) {
}
