package com.iasdlc.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Lo estable: nombre, descripción y squad. El proyecto no recorre fases ni tiene historias;
 * eso lo hacen sus iniciativas.
 */
public class Project {

    private final UUID id;
    private final String name;
    private final String description;
    private final UUID squadId;
    private final Instant createdAt;

    public Project(UUID id, String name, String description, UUID squadId, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.squadId = squadId;
        this.createdAt = createdAt;
    }

    public static Project create(String name, String description, UUID squadId, Instant now) {
        return new Project(UUID.randomUUID(), name.trim(), description, squadId, now);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public UUID squadId() { return squadId; }
    public Instant createdAt() { return createdAt; }
}
