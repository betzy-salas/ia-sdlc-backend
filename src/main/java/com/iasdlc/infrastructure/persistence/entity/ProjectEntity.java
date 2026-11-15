package com.iasdlc.infrastructure.persistence.entity;

import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.PhaseProgress;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    String description;

    @Column(name = "squad_id", nullable = false)
    UUID squadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false)
    PhaseCode currentPhase;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortIndex ASC")
    List<StoryEntity> stories = new ArrayList<>();

    /** Avance por fase (tabla project_phases). */
    @ElementCollection
    @CollectionTable(name = "project_phases", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "phase_code")
    @MapKeyEnumerated(EnumType.STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "progress", nullable = false)
    Map<PhaseCode, PhaseProgress> phases = new HashMap<>();
}
