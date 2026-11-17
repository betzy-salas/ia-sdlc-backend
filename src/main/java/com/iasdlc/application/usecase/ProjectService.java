package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.ProjectUseCase;
import com.iasdlc.application.port.out.InitiativeRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.view.InitiativeView;
import com.iasdlc.application.view.ProjectView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@UseCase
@Transactional
public class ProjectService implements ProjectUseCase {

    private final ProjectRepository projects;
    private final InitiativeRepository initiatives;
    private final SquadRepository squads;
    private final Clock clock;

    public ProjectService(ProjectRepository projects, InitiativeRepository initiatives, SquadRepository squads,
                          Clock clock) {
        this.projects = projects;
        this.initiatives = initiatives;
        this.squads = squads;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectView> listProjects() {
        return projects.findAll().stream()
                .map(project -> view(project, squadOf(project)))
                .toList();
    }

    @Override
    public ProjectView createProject(String name, String description, UUID squadId) {
        if (projects.existsByName(name.trim())) {
            throw new DomainException(ErrorCode.DUPLICATE_NAME, "Ya existe un proyecto con ese nombre");
        }
        Squad squad = squads.findById(squadId).orElseThrow(() -> DomainException.notFound("el squad"));
        Project project = projects.save(Project.create(name, description, squad.id(), clock.instant()));
        return view(project, squad);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectView getProject(UUID projectId) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> DomainException.notFound("el proyecto"));
        return view(project, squadOf(project));
    }

    private Squad squadOf(Project project) {
        return squads.findById(project.squadId())
                .orElseThrow(() -> new IllegalStateException("El proyecto apunta a un squad inexistente"));
    }

    /** Las iniciativas van como resumen (sin recorrido), de la más reciente a la más antigua. */
    private ProjectView view(Project project, Squad squad) {
        List<InitiativeView> summaries = initiatives.findByProjectId(project.id()).stream()
                .map(initiative -> new InitiativeView(initiative, project, squad, List.of()))
                .toList();
        return new ProjectView(project, squad, summaries);
    }
}
