package com.iasdlc.domain.model;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Squad {

    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private final List<SquadMember> members;

    public Squad(UUID id, String name, Instant createdAt, List<SquadMember> members) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.members = new ArrayList<>(members);
    }

    public static Squad create(String name, Instant now) {
        return new Squad(UUID.randomUUID(), name.trim(), now, List.of());
    }

    /**
     * Agrega un integrante. Un usuario puede tener varios roles en el squad,
     * pero el mismo rol una sola vez (sea titular o suplente).
     */
    public SquadMember addMember(UUID userId, SquadRole role, boolean substitute) {
        if (hasRole(userId, role)) {
            throw new DomainException(ErrorCode.DUPLICATE_MEMBER, "El usuario ya tiene ese rol en el squad");
        }
        SquadMember member = new SquadMember(UUID.randomUUID(), userId, role, substitute);
        members.add(member);
        return member;
    }

    public void removeMember(UUID memberId) {
        boolean removed = members.removeIf(m -> m.id().equals(memberId));
        if (!removed) {
            throw DomainException.notFound("el integrante en el squad");
        }
    }

    /** Indica si el usuario tiene el rol en el squad, como titular o como suplente. */
    public boolean hasRole(UUID userId, SquadRole role) {
        return members.stream().anyMatch(m -> m.userId().equals(userId) && m.role() == role);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public Instant createdAt() { return createdAt; }
    public List<SquadMember> members() { return List.copyOf(members); }
}
