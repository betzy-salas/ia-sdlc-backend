package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.ProjectView;
import com.iasdlc.domain.model.Project;
import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectSummaryResponse;

public final class ProjectWebMapper {

    private ProjectWebMapper() {
    }

    public static ProjectSummaryResponse toSummary(ProjectView view) {
        Project project = view.project();
        return new ProjectSummaryResponse(project.id(), project.name(), project.description(), squadRef(view),
                view.initiatives().size());
    }

    public static ProjectResponse toResponse(ProjectView view) {
        Project project = view.project();
        return new ProjectResponse(project.id(), project.name(), project.description(), squadRef(view),
                view.initiatives().stream().map(InitiativeWebMapper::toSummary).toList());
    }

    private static NamedRef squadRef(ProjectView view) {
        return new NamedRef(view.squad().id(), view.squad().name());
    }
}
