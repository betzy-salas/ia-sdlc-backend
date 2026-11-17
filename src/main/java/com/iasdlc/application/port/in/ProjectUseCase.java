package com.iasdlc.application.port.in;

import com.iasdlc.application.view.ProjectView;

import java.util.List;
import java.util.UUID;

public interface ProjectUseCase {
    List<ProjectView> listProjects();
    ProjectView createProject(String name, String description, UUID squadId);
    ProjectView getProject(UUID projectId);
}
