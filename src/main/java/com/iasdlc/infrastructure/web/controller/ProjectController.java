package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.ExecutionUseCase;
import com.iasdlc.application.port.in.ProjectUseCase;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.CreateProjectRequest;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.CreateStoryRequest;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectSummaryResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.StoryResponse;
import com.iasdlc.infrastructure.web.mapper.ExecutionWebMapper;
import com.iasdlc.infrastructure.web.mapper.ProjectWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectUseCase projects;
    private final ExecutionUseCase executions;

    public ProjectController(ProjectUseCase projects, ExecutionUseCase executions) {
        this.projects = projects;
        this.executions = executions;
    }

    @GetMapping
    public List<ProjectSummaryResponse> list() {
        return projects.listProjects().stream().map(ProjectWebMapper::toSummary).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return ProjectWebMapper.toResponse(
                projects.createProject(request.name(), request.description(), request.squadId()));
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable("projectId") UUID projectId) {
        return ProjectWebMapper.toResponse(projects.getProject(projectId));
    }

    @PostMapping("/{projectId}/stories")
    @ResponseStatus(HttpStatus.CREATED)
    public StoryResponse addStory(@PathVariable("projectId") UUID projectId,
                                  @Valid @RequestBody CreateStoryRequest request) {
        return ProjectWebMapper.toResponse(
                projects.addStory(projectId, request.title(), request.description(), request.origin()));
    }

    @DeleteMapping("/{projectId}/stories/{storyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStory(@PathVariable("projectId") UUID projectId, @PathVariable("storyId") UUID storyId) {
        projects.removeStory(projectId, storyId);
    }

    /**
     * Ejecuta el agente de la fase sobre las historias del proyecto. En esta entrega solo se
     * admite REQUIREMENTS; la ejecución es síncrona y no lleva cuerpo.
     */
    @PostMapping("/{projectId}/phases/{phaseCode}/executions")
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionResponse runPhase(@PathVariable("projectId") UUID projectId,
                                      @PathVariable("phaseCode") PhaseCode phaseCode,
                                      @AuthenticationPrincipal UUID userId) {
        return ExecutionWebMapper.toResponse(executions.runPhase(projectId, phaseCode, userId));
    }
}
