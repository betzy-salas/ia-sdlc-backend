package com.iasdlc.application.port.in;

import com.iasdlc.application.view.AgentView;
import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;

import java.util.List;
import java.util.UUID;

public interface AgentUseCase {
    /** Lista agentes; los filtros nulos no se aplican. */
    List<AgentView> listAgents(PhaseCode phase, AgentStatus status);
    AgentView createAgent(CreateAgentCommand command);
    AgentView getAgent(UUID agentId);
    AgentView addVersion(UUID agentId, String version, String commitSha, String instructions, String changeNotes);
    AgentView changeStatus(UUID agentId, AgentStatus status);

    /** {@code ownerUserId} nulo significa "el usuario que crea el agente". */
    record CreateAgentCommand(String name, PhaseCode phaseCode, String description, String repositoryUrl,
                              String repositoryPath, UUID ownerUserId, UUID requestedBy) {
    }
}
