package com.iasdlc.application.view;

import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.Phase;

import java.util.List;

public record DashboardPhaseView(Phase phase, List<Agent> agents, long projectsInPhase, long pendingApprovals) {
}
