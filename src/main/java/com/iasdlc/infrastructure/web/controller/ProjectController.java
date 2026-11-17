package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.InitiativeUseCase;
import com.iasdlc.application.port.in.ProjectUseCase;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.CreateInitiativeRequest;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.InitiativeResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.CreateProjectRequest;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectResponse;
import com.iasdlc.infrastructure.web.dto.ProjectDtos.ProjectSummaryResponse;
import com.iasdlc.infrastructure.web.mapper.InitiativeWebMapper;
import com.iasdlc.infrastructure.web.mapper.ProjectWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Proyectos: lo estable (nombre, descripción y squad). Las historias, las ejecuciones y los
 * gates cuelgan de las iniciativas (ver InitiativeController).
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectUseCase projects;
    private final InitiativeUseCase initiatives;

    public ProjectController(ProjectUseCase projects, InitiativeUseCase initiatives) {
        this.projects = projects;
        this.initiatives = initiatives;
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

    @PostMapping("/{projectId}/initiatives")
    @ResponseStatus(HttpStatus.CREATED)
    public InitiativeResponse createInitiative(@PathVariable("projectId") UUID projectId,
                                               @Valid @RequestBody CreateInitiativeRequest request) {
        return InitiativeWebMapper.toResponse(
                initiatives.createInitiative(projectId, request.name(), request.description()));
    }
}
