package com.iasdlc.infrastructure.web.dto;

import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.SquadRole;

import java.util.List;
import java.util.UUID;

public final class PhaseDtos {

    private PhaseDtos() {
    }

    public record PhaseResponse(PhaseCode code, int order, String name, SquadRole approverRole,
                                List<PhaseAgentResponse> agents) {
    }

    public record PhaseAgentResponse(UUID id, String name, AgentStatus status, String currentVersion) {
    }

    /** Elemento de GET /dashboard/phases. */
    public record DashboardPhaseResponse(PhaseCode phaseCode, int order, String name, SquadRole approverRole,
                                         List<DashboardAgentResponse> agents, long projectsInPhase,
                                         long pendingApprovals) {
    }

    public record DashboardAgentResponse(UUID id, String name, AgentStatus status) {
    }
}
