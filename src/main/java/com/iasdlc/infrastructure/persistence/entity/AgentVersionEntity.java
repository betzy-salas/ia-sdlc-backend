package com.iasdlc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_versions")
public class AgentVersionEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    AgentEntity agent;

    @Column(nullable = false)
    String version;

    @Column(name = "commit_sha")
    String commitSha;

    @Column(nullable = false)
    String instructions;

    @Column(name = "change_notes")
    String changeNotes;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "sort_index", nullable = false)
    int sortIndex;
}
