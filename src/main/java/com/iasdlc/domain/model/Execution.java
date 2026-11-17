package com.iasdlc.domain.model;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Ejecución de un agente sobre las historias de una iniciativa en una fase. Es un registro histórico:
 * no se edita ni se borra; solo avanza de estado (RUNNING -> PENDING_APPROVAL/FAILED -> decisión).
 */
public class Execution {

    private final UUID id;
    private final UUID initiativeId;
    private final PhaseCode phaseCode;
    private final UUID agentId;
    private final String agentName;
    private final String agentVersion;
    private final UUID launchedBy;
    private final Instant createdAt;
    private ExecutionStatus status;
    private List<StoryRefinement> refinedStories;
    private GateDecision decision;
    private String failureReason;

    public Execution(UUID id, UUID initiativeId, PhaseCode phaseCode, UUID agentId, String agentName,
                     String agentVersion, UUID launchedBy, ExecutionStatus status, Instant createdAt,
                     List<StoryRefinement> refinedStories, GateDecision decision, String failureReason) {
        this.id = id;
        this.initiativeId = initiativeId;
        this.phaseCode = phaseCode;
        this.agentId = agentId;
        this.agentName = agentName;
        this.agentVersion = agentVersion;
        this.launchedBy = launchedBy;
        this.status = status;
        this.createdAt = createdAt;
        this.refinedStories = List.copyOf(refinedStories);
        this.decision = decision;
        this.failureReason = failureReason;
    }

    public static Execution start(UUID initiativeId, PhaseCode phaseCode, Agent agent, String agentVersion,
                                  UUID launchedBy, Instant now) {
        return new Execution(UUID.randomUUID(), initiativeId, phaseCode, agent.id(), agent.name(), agentVersion,
                launchedBy, ExecutionStatus.RUNNING, now, List.of(), null, null);
    }

    /** El agente terminó: el resultado queda pendiente de aprobación. */
    public void complete(List<StoryRefinement> result) {
        ensureRunning();
        this.refinedStories = List.copyOf(result);
        this.status = ExecutionStatus.PENDING_APPROVAL;
    }

    public void fail(String reason) {
        ensureRunning();
        this.failureReason = reason;
        this.status = ExecutionStatus.FAILED;
    }

    /**
     * Registra la decisión del gate. Solo se decide una vez y solo sobre ejecuciones pendientes;
     * el reproceso y el rechazo exigen comentario.
     */
    public void decide(Decision gateDecision, UUID decidedBy, String comment, Instant now) {
        if (status != ExecutionStatus.PENDING_APPROVAL) {
            throw new DomainException(ErrorCode.EXECUTION_ALREADY_DECIDED,
                    "La ejecución ya fue decidida o no está pendiente de aprobación");
        }
        String cleanComment = comment == null || comment.isBlank() ? null : comment.trim();
        if (gateDecision != Decision.APPROVE && cleanComment == null) {
            throw new DomainException(ErrorCode.COMMENT_REQUIRED,
                    "El comentario es obligatorio para pedir reproceso o rechazar");
        }
        this.decision = new GateDecision(gateDecision, decidedBy, now, cleanComment);
        this.status = switch (gateDecision) {
            case APPROVE -> ExecutionStatus.ACCEPTED;
            case REQUEST_REWORK -> ExecutionStatus.REWORK_REQUESTED;
            case REJECT -> ExecutionStatus.REJECTED;
        };
    }

    /** Indica si el resultado sigue siendo válido para mostrarse en las historias (no rechazado ni fallido). */
    public boolean hasValidResult() {
        return status == ExecutionStatus.PENDING_APPROVAL
                || status == ExecutionStatus.ACCEPTED
                || status == ExecutionStatus.REWORK_REQUESTED;
    }

    private void ensureRunning() {
        if (status != ExecutionStatus.RUNNING) {
            throw new IllegalStateException("La ejecución ya terminó");
        }
    }

    public UUID id() { return id; }
    public UUID initiativeId() { return initiativeId; }
    public PhaseCode phaseCode() { return phaseCode; }
    public UUID agentId() { return agentId; }
    public String agentName() { return agentName; }
    public String agentVersion() { return agentVersion; }
    public UUID launchedBy() { return launchedBy; }
    public ExecutionStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public List<StoryRefinement> refinedStories() { return refinedStories; }
    public GateDecision decision() { return decision; }
    public String failureReason() { return failureReason; }
}
