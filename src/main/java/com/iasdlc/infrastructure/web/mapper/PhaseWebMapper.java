package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.DashboardPhaseView;
import com.iasdlc.application.view.PhaseView;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.infrastructure.web.dto.PhaseDtos.DashboardAgentResponse;
import com.iasdlc.infrastructure.web.dto.PhaseDtos.DashboardPhaseResponse;
import com.iasdlc.infrastructure.web.dto.PhaseDtos.PhaseAgentResponse;
import com.iasdlc.infrastructure.web.dto.PhaseDtos.PhaseResponse;

public final class PhaseWebMapper {

    private PhaseWebMapper() {
    }

    public static PhaseResponse toResponse(PhaseView view) {
        Phase phase = view.phase();
        return new PhaseResponse(phase.code(), phase.order(), phase.name(), phase.approverRole(),
                view.agents().stream()
                        .map(a -> new PhaseAgentResponse(a.id(), a.name(), a.status(), a.currentVersion()))
                        .toList());
    }

    public static DashboardPhaseResponse toResponse(DashboardPhaseView view) {
        Phase phase = view.phase();
        return new DashboardPhaseResponse(phase.code(), phase.order(), phase.name(), phase.approverRole(),
                view.agents().stream()
                        .map(a -> new DashboardAgentResponse(a.id(), a.name(), a.status()))
                        .toList(),
                view.initiativesInPhase(), view.pendingApprovals());
    }
}
