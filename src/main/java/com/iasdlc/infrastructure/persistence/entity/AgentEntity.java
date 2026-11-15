package com.iasdlc.infrastructure.persistence.entity;

import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_code", nullable = false)
    PhaseCode phaseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AgentStatus status;

    @Column(name = "current_version")
    String currentVersion;

    String description;

    @Column(name = "repository_url")
    String repositoryUrl;

    @Column(name = "repository_path")
    String repositoryPath;

    @Column(name = "owner_user_id")
    UUID ownerUserId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortIndex ASC")
    List<AgentVersionEntity> versions = new ArrayList<>();
}
