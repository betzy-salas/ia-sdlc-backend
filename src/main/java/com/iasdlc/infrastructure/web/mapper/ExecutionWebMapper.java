package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.GateDecision;
import com.iasdlc.domain.model.RefinedStory;
import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.CommonDtos.PersonRef;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.DecisionResponse;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionAgentRef;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionResponse;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionSummaryResponse;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.RefinedStoryItemResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.RefinedStoryResponse;

public final class ExecutionWebMapper {

    private ExecutionWebMapper() {
    }

    public static ExecutionSummaryResponse toSummary(ExecutionView view) {
        Execution e = view.execution();
        return new ExecutionSummaryResponse(e.id(), new NamedRef(e.projectId(), view.projectName()),
                e.phaseCode(), new ExecutionAgentRef(e.agentId(), e.agentName(), e.agentVersion()),
                new PersonRef(e.launchedBy(), view.launchedByName()), e.status(), e.createdAt());
    }

    public static ExecutionResponse toResponse(ExecutionView view) {
        Execution e = view.execution();
        GateDecision decision = e.decision();
        DecisionResponse decisionResponse = decision == null ? null
                : new DecisionResponse(decision.decision(),
                        new PersonRef(decision.decidedBy(), view.decidedByName()),
                        decision.decidedAt(), decision.comment());
        return new ExecutionResponse(e.id(), new NamedRef(e.projectId(), view.projectName()), e.phaseCode(),
                new ExecutionAgentRef(e.agentId(), e.agentName(), e.agentVersion()),
                new PersonRef(e.launchedBy(), view.launchedByName()), e.status(), e.createdAt(),
                e.refinedStories().stream()
                        .map(r -> new RefinedStoryItemResponse(r.storyId(), r.originalTitle(),
                                toResponse(r.refined())))
                        .toList(),
                decisionResponse);
    }

    public static RefinedStoryResponse toResponse(RefinedStory refined) {
        if (refined == null) {
            return null;
        }
        return new RefinedStoryResponse(refined.title(), refined.asA(), refined.iWant(), refined.soThat(),
                refined.acceptanceCriteria(), refined.ambiguities());
    }
}
