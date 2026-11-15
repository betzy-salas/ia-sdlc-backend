package com.iasdlc.domain.model;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Agent {

    private final UUID id;
    private final String name;
    private final PhaseCode phaseCode;
    private AgentStatus status;
    private String currentVersion;
    private final String description;
    private final String repositoryUrl;
    private final String repositoryPath;
    private final UUID ownerUserId;
    private final Instant createdAt;
    private final List<AgentVersion> versions;

    public Agent(UUID id, String name, PhaseCode phaseCode, AgentStatus status, String currentVersion,
                 String description, String repositoryUrl, String repositoryPath, UUID ownerUserId,
                 Instant createdAt, List<AgentVersion> versions) {
        this.id = id;
        this.name = name;
        this.phaseCode = phaseCode;
        this.status = status;
        this.currentVersion = currentVersion;
        this.description = description;
        this.repositoryUrl = repositoryUrl;
        this.repositoryPath = repositoryPath;
        this.ownerUserId = ownerUserId;
        this.createdAt = createdAt;
        this.versions = new ArrayList<>(versions);
    }

    /** Un agente nuevo queda PLANNED y sin versión vigente. */
    public static Agent create(String name, PhaseCode phaseCode, String description, String repositoryUrl,
                               String repositoryPath, UUID ownerUserId, Instant now) {
        return new Agent(UUID.randomUUID(), name.trim(), phaseCode, AgentStatus.PLANNED, null,
                description, repositoryUrl, repositoryPath, ownerUserId, now, List.of());
    }

    /** Registra una versión y la deja como vigente. No se permite repetir el número de versión. */
    public AgentVersion addVersion(String version, String commitSha, String instructions, String changeNotes,
                                   Instant now) {
        String normalized = version.trim();
        if (versions.stream().anyMatch(v -> v.version().equals(normalized))) {
            throw new DomainException(ErrorCode.DUPLICATE_NAME,
                    "El agente ya tiene registrada la versión " + normalized);
        }
        AgentVersion created = new AgentVersion(UUID.randomUUID(), normalized, commitSha, instructions,
                changeNotes, now);
        versions.add(created);
        currentVersion = normalized;
        return created;
    }

    public void changeStatus(AgentStatus newStatus) {
        this.status = newStatus;
    }

    /** Un agente se puede ejecutar si está en PILOT o ACTIVE y tiene versión vigente. */
    public boolean isExecutable() {
        return (status == AgentStatus.PILOT || status == AgentStatus.ACTIVE) && currentVersionDetail().isPresent();
    }

    public Optional<AgentVersion> currentVersionDetail() {
        if (currentVersion == null) {
            return Optional.empty();
        }
        return versions.stream().filter(v -> v.version().equals(currentVersion)).findFirst();
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public PhaseCode phaseCode() { return phaseCode; }
    public AgentStatus status() { return status; }
    public String currentVersion() { return currentVersion; }
    public String description() { return description; }
    public String repositoryUrl() { return repositoryUrl; }
    public String repositoryPath() { return repositoryPath; }
    public UUID ownerUserId() { return ownerUserId; }
    public Instant createdAt() { return createdAt; }
    public List<AgentVersion> versions() { return List.copyOf(versions); }
}
