package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.InitiativeUseCase;
import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.application.port.out.InitiativeRepository;
import com.iasdlc.application.port.out.PhaseRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.application.view.InitiativeView;
import com.iasdlc.application.view.PhaseStepView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryOrigin;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@UseCase
@Transactional
public class InitiativeService implements InitiativeUseCase {

    private final InitiativeRepository initiatives;
    private final ProjectRepository projects;
    private final SquadRepository squads;
    private final PhaseRepository phases;
    private final ExecutionRepository executions;
    private final Clock clock;
    private final ViewAssembler views;

    public InitiativeService(InitiativeRepository initiatives, ProjectRepository projects, SquadRepository squads,
                             PhaseRepository phases, ExecutionRepository executions, UserRepository users,
                             Clock clock) {
        this.initiatives = initiatives;
        this.projects = projects;
        this.squads = squads;
        this.phases = phases;
        this.executions = executions;
        this.clock = clock;
        this.views = new ViewAssembler(users, squads);
    }

    @Override
    public InitiativeView createInitiative(UUID projectId, String name, String description) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> DomainException.notFound("el proyecto"));
        if (initiatives.existsByProjectIdAndName(project.id(), name.trim())) {
            throw new DomainException(ErrorCode.DUPLICATE_NAME,
                    "Ya existe una iniciativa con ese nombre en el proyecto");
        }
        Initiative initiative = initiatives.save(
                Initiative.create(project.id(), name, description, clock.instant()));
        return detail(initiative, project, squadOf(project));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InitiativeView> listInitiatives(UUID projectId, PhaseCode phase) {
        List<Initiative> found = projectId == null ? initiatives.findAll() : initiatives.findByProjectId(projectId);
        // El listado es un resumen: no necesita el recorrido por fases.
        Map<UUID, Project> projectsById = new HashMap<>();
        Map<UUID, Squad> squadsById = new HashMap<>();
        List<InitiativeView> result = new ArrayList<>();
        for (Initiative initiative : found) {
            if (phase != null && initiative.currentPhase() != phase) {
                continue;
            }
            Project project = projectsById.computeIfAbsent(initiative.projectId(), id -> projectOf(initiative));
            Squad squad = squadsById.computeIfAbsent(project.squadId(), id -> squadOf(project));
            result.add(new InitiativeView(initiative, project, squad, List.of()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public InitiativeView getInitiative(UUID initiativeId) {
        Initiative initiative = load(initiativeId);
        Project project = projectOf(initiative);
        return detail(initiative, project, squadOf(project));
    }

    @Override
    public Story addStory(UUID initiativeId, String title, String description, StoryOrigin origin) {
        Initiative initiative = load(initiativeId);
        Story story = initiative.addStory(title, description, origin, clock.instant());
        initiatives.save(initiative);
        return story;
    }

    @Override
    public void removeStory(UUID initiativeId, UUID storyId) {
        Initiative initiative = load(initiativeId);
        initiative.removeStory(storyId);
        initiatives.save(initiative);
    }

    private Initiative load(UUID initiativeId) {
        return initiatives.findById(initiativeId).orElseThrow(() -> DomainException.notFound("la iniciativa"));
    }

    private Project projectOf(Initiative initiative) {
        return projects.findById(initiative.projectId())
                .orElseThrow(() -> new IllegalStateException("La iniciativa apunta a un proyecto inexistente"));
    }

    private Squad squadOf(Project project) {
        return squads.findById(project.squadId())
                .orElseThrow(() -> new IllegalStateException("El proyecto apunta a un squad inexistente"));
    }

    private InitiativeView detail(Initiative initiative, Project project, Squad squad) {
        // Vienen de la más reciente a la más antigua: la primera de cada fase es la última ejecución.
        List<Execution> initiativeExecutions = executions.findByInitiativeId(initiative.id());
        List<PhaseStepView> journey = new ArrayList<>();
        for (Phase phase : phases.findAll()) {
            ExecutionView last = initiativeExecutions.stream()
                    .filter(e -> e.phaseCode() == phase.code())
                    .findFirst()
                    .map(e -> views.execution(e, initiative, project))
                    .orElse(null);
            journey.add(new PhaseStepView(phase, initiative.progressOf(phase.code()), last));
        }
        return new InitiativeView(initiative, project, squad, journey);
    }
}
