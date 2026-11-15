package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.ProjectUseCase;
import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.application.port.out.PhaseRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.application.view.PhaseStepView;
import com.iasdlc.application.view.ProjectView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryOrigin;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UseCase
@Transactional
public class ProjectService implements ProjectUseCase {

    private final ProjectRepository projects;
    private final SquadRepository squads;
    private final PhaseRepository phases;
    private final ExecutionRepository executions;
    private final Clock clock;
    private final ViewAssembler views;

    public ProjectService(ProjectRepository projects, SquadRepository squads, PhaseRepository phases,
                          ExecutionRepository executions, UserRepository users, Clock clock) {
        this.projects = projects;
        this.squads = squads;
        this.phases = phases;
        this.executions = executions;
        this.clock = clock;
        this.views = new ViewAssembler(users, squads);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectView> listProjects() {
        // El listado es un resumen: no necesita el recorrido por fases.
        return projects.findAll().stream()
                .map(project -> new ProjectView(project, squadOf(project), List.of()))
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
        Project project = load(projectId);
        return view(project, squadOf(project));
    }

    @Override
    public Story addStory(UUID projectId, String title, String description, StoryOrigin origin) {
        Project project = load(projectId);
        Story story = project.addStory(title, description, origin, clock.instant());
        projects.save(project);
        return story;
    }

    @Override
    public void removeStory(UUID projectId, UUID storyId) {
        Project project = load(projectId);
        project.removeStory(storyId);
        projects.save(project);
    }

    private Project load(UUID projectId) {
        return projects.findById(projectId).orElseThrow(() -> DomainException.notFound("el proyecto"));
    }

    private Squad squadOf(Project project) {
        return squads.findById(project.squadId())
                .orElseThrow(() -> new IllegalStateException("El proyecto apunta a un squad inexistente"));
    }

    private ProjectView view(Project project, Squad squad) {
        // Vienen de la más reciente a la más antigua: la primera de cada fase es la última ejecución.
        List<Execution> projectExecutions = executions.findByProjectId(project.id());
        List<PhaseStepView> journey = new ArrayList<>();
        for (Phase phase : phases.findAll()) {
            ExecutionView last = projectExecutions.stream()
                    .filter(e -> e.phaseCode() == phase.code())
                    .findFirst()
                    .map(e -> views.execution(e, project.name()))
                    .orElse(null);
            journey.add(new PhaseStepView(phase, project.progressOf(phase.code()), last));
        }
        return new ProjectView(project, squad, journey);
    }
}
