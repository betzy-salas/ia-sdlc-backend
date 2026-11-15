package com.iasdlc.application.port.in;

import com.iasdlc.application.view.ProjectView;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryOrigin;

import java.util.List;
import java.util.UUID;

public interface ProjectUseCase {
    List<ProjectView> listProjects();
    ProjectView createProject(String name, String description, UUID squadId);
    ProjectView getProject(UUID projectId);
    Story addStory(UUID projectId, String title, String description, StoryOrigin origin);
    void removeStory(UUID projectId, UUID storyId);
}
