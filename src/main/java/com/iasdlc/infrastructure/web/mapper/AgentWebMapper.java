package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.AgentView;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.infrastructure.web.dto.AgentDtos.AgentResponse;
import com.iasdlc.infrastructure.web.dto.AgentDtos.AgentVersionResponse;
import com.iasdlc.infrastructure.web.dto.CommonDtos.PersonRef;

public final class AgentWebMapper {

    private AgentWebMapper() {
    }

    /** Para el listado: las versiones van sin {@code instructions}. */
    public static AgentResponse toListItem(AgentView view) {
        return toResponse(view, false);
    }

    /** Para un agente individual: cada versión incluye {@code instructions}. */
    public static AgentResponse toDetail(AgentView view) {
        return toResponse(view, true);
    }

    private static AgentResponse toResponse(AgentView view, boolean withInstructions) {
        Agent agent = view.agent();
        PersonRef owner = view.owner() == null ? null
                : new PersonRef(view.owner().id(), view.owner().fullName());
        return new AgentResponse(agent.id(), agent.name(), agent.phaseCode(), agent.status(),
                agent.currentVersion(), agent.description(), agent.repositoryUrl(), agent.repositoryPath(),
                owner,
                agent.versions().stream()
                        .map(v -> new AgentVersionResponse(v.version(), v.commitSha(), v.changeNotes(),
                                v.createdAt(), withInstructions ? v.instructions() : null))
                        .toList());
    }
}
