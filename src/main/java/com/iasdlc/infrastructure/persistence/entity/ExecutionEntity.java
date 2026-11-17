package com.iasdlc.infrastructure.persistence.entity;

import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.PhaseCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "executions")
public class ExecutionEntity {

    @Id
    UUID id;

    @Column(name = "initiative_id", nullable = false, updatable = false)
    UUID initiativeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_code", nullable = false, updatable = false)
    PhaseCode phaseCode;

    @Column(name = "agent_id", nullable = false, updatable = false)
    UUID agentId;

    @Column(name = "agent_name", nullable = false, updatable = false)
    String agentName;

    @Column(name = "agent_version", nullable = false, updatable = false)
    String agentVersion;

    @Column(name = "launched_by", nullable = false, updatable = false)
    UUID launchedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ExecutionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    /** Resultado del agente serializado como JSON (lista de StoryRefinement). */
    @Column(name = "refined_stories_json", nullable = false, updatable = false)
    String refinedStoriesJson;

    @Column(name = "failure_reason", updatable = false)
    String failureReason;

    @Enumerated(EnumType.STRING)
    Decision decision;

    @Column(name = "decided_by")
    UUID decidedBy;

    @Column(name = "decided_at")
    Instant decidedAt;

    @Column(name = "decision_comment")
    String decisionComment;
}
