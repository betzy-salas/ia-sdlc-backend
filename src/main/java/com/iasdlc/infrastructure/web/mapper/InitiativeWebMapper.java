package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.InitiativeView;
import com.iasdlc.application.view.PhaseStepView;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Story;
import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.InitiativeResponse;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.InitiativeSummaryResponse;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.PhaseStepResponse;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.StoryResponse;

public final class InitiativeWebMapper {

    private InitiativeWebMapper() {
    }

    public static InitiativeSummaryResponse toSummary(InitiativeView view) {
        Initiative initiative = view.initiative();
        return new InitiativeSummaryResponse(initiative.id(), initiative.name(), projectRef(view), squadRef(view),
                initiative.currentPhase(), initiative.completed(), initiative.stories().size(),
                initiative.createdAt());
    }

    public static InitiativeResponse toResponse(InitiativeView view) {
        Initiative initiative = view.initiative();
        return new InitiativeResponse(initiative.id(), initiative.name(), initiative.description(),
                projectRef(view), squadRef(view), initiative.currentPhase(), initiative.completed(),
                initiative.stories().stream().map(InitiativeWebMapper::toResponse).toList(),
                view.journey().stream().map(InitiativeWebMapper::toResponse).toList(),
                initiative.createdAt());
    }

    public static StoryResponse toResponse(Story story) {
        return new StoryResponse(story.id(), story.title(), story.description(), story.origin(),
                ExecutionWebMapper.toResponse(story.refined()));
    }

    private static PhaseStepResponse toResponse(PhaseStepView step) {
        return new PhaseStepResponse(step.phase().code(), step.phase().order(), step.phase().name(),
                step.progress(), step.phase().approverRole(),
                step.lastExecution() == null ? null : ExecutionWebMapper.toSummary(step.lastExecution()));
    }

    private static NamedRef projectRef(InitiativeView view) {
        return new NamedRef(view.project().id(), view.project().name());
    }

    private static NamedRef squadRef(InitiativeView view) {
        return new NamedRef(view.squad().id(), view.squad().name());
    }
}
