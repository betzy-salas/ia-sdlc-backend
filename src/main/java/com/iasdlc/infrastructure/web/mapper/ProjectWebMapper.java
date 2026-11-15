package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.PhaseStepView;
import com.iasdlc.application.view.ProjectView;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Story;
import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.PhaseStepResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectSummaryResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.StoryResponse;

public final class ProjectWebMapper {

    private ProjectWebMapper() {
    }

    public static ProjectSummaryResponse toSummary(ProjectView view) {
        Project project = view.project();
        return new ProjectSummaryResponse(project.id(), project.name(), squadRef(view), project.currentPhase(),
                project.stories().size());
    }

    public static ProjectResponse toResponse(ProjectView view) {
        Project project = view.project();
        return new ProjectResponse(project.id(), project.name(), project.description(), squadRef(view),
                project.currentPhase(),
                project.stories().stream().map(ProjectWebMapper::toResponse).toList(),
                view.journey().stream().map(ProjectWebMapper::toResponse).toList());
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

    private static NamedRef squadRef(ProjectView view) {
        return new NamedRef(view.squad().id(), view.squad().name());
    }
}
