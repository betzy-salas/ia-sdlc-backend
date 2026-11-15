package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.ExecutionUseCase;
import com.iasdlc.application.port.out.AgentDefinition;
import com.iasdlc.application.port.out.AgentInput;
import com.iasdlc.application.port.out.AgentOutput;
import com.iasdlc.application.port.out.AgentRepository;
import com.iasdlc.application.port.out.AgentRunner;
import com.iasdlc.application.port.out.AgentRunnerException;
import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.AgentVersion;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.RefinedStory;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryRefinement;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@UseCase
@Transactional
public class ExecutionService implements ExecutionUseCase {

    private final ProjectRepository projects;
    private final AgentRepository agents;
    private final ExecutionRepository executions;
    private final AgentRunner agentRunner;
    private final Clock clock;
    private final ViewAssembler views;

    public ExecutionService(ProjectRepository projects, AgentRepository agents, ExecutionRepository executions,
                            UserRepository users, SquadRepository squads, AgentRunner agentRunner,
                            Clock clock) {
        this.projects = projects;
        this.agents = agents;
        this.executions = executions;
        this.agentRunner = agentRunner;
        this.clock = clock;
        this.views = new ViewAssembler(users, squads);
    }

    @Override
    public ExecutionView runPhase(UUID projectId, PhaseCode phaseCode, UUID launchedBy) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> DomainException.notFound("el proyecto"));
        if (phaseCode != PhaseCode.REQUIREMENTS) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR,
                    "En esta entrega solo se puede ejecutar la fase REQUIREMENTS");
        }
        project.ensureExecutable(phaseCode);
        Agent agent = selectAgent(phaseCode);
        AgentVersion version = agent.currentVersionDetail().orElseThrow();

        Execution execution = Execution.start(project.id(), phaseCode, agent, version.version(), launchedBy,
                clock.instant());
        try {
            AgentOutput output = agentRunner.run(definitionOf(agent, version), inputOf(project));
            List<StoryRefinement> refinements = refinementsOf(project, output);
            execution.complete(refinements);
            project.awaitGate(phaseCode, refinements);
            projects.save(project);
        } catch (AgentRunnerException e) {
            // La ejecución queda registrada como fallida y la fase sigue en curso para reintentar.
            execution.fail(e.getMessage());
        }
        return views.execution(executions.save(execution), project.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionView> listExecutions(UUID projectId, ExecutionStatus status) {
        List<Execution> found = projectId == null ? executions.findAll() : executions.findByProjectId(projectId);
        Map<UUID, String> projectNames = projects.findAll().stream()
                .collect(Collectors.toMap(Project::id, Project::name));
        return found.stream()
                .filter(e -> status == null || e.status() == status)
                .map(e -> views.execution(e, projectNames.get(e.projectId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionView getExecution(UUID executionId) {
        Execution execution = executions.findById(executionId)
                .orElseThrow(() -> DomainException.notFound("la ejecución"));
        String projectName = projects.findById(execution.projectId()).map(Project::name).orElse(null);
        return views.execution(execution, projectName);
    }

    /** Agente ejecutable de la fase; si hay varios se prefiere ACTIVE sobre PILOT y luego por nombre. */
    private Agent selectAgent(PhaseCode phaseCode) {
        return agents.findAll().stream()
                .filter(a -> a.phaseCode() == phaseCode && a.isExecutable())
                .min(Comparator.comparing((Agent a) -> a.status() == AgentStatus.ACTIVE ? 0 : 1)
                        .thenComparing(Agent::name))
                .orElseThrow(() -> new DomainException(ErrorCode.AGENT_NOT_AVAILABLE,
                        "No hay un agente en PILOT o ACTIVE con versión vigente para la fase " + phaseCode));
    }

    private static AgentDefinition definitionOf(Agent agent, AgentVersion version) {
        return new AgentDefinition(agent.id(), agent.name(), agent.phaseCode(), version.version(),
                version.instructions(), agent.repositoryUrl(), agent.repositoryPath());
    }

    private static AgentInput inputOf(Project project) {
        List<AgentInput.StoryInput> stories = project.stories().stream()
                .map(s -> new AgentInput.StoryInput(s.id(), s.title(), s.description()))
                .toList();
        return new AgentInput(project.id(), project.name(), project.description(), stories);
    }

    /** Empareja la salida del agente con las historias; exige un resultado por cada historia. */
    private static List<StoryRefinement> refinementsOf(Project project, AgentOutput output) {
        if (output == null || output.refinedStories() == null) {
            throw new AgentRunnerException("El agente no devolvió ningún resultado");
        }
        List<StoryRefinement> refinements = new ArrayList<>();
        for (Story story : project.stories()) {
            RefinedStory refined = output.refinedStories().stream()
                    .filter(r -> story.id().equals(r.storyId()) && r.refined() != null)
                    .map(AgentOutput.RefinedStoryOutput::refined)
                    .findFirst()
                    .orElseThrow(() -> new AgentRunnerException(
                            "El agente no devolvió resultado para la historia \"" + story.title() + "\""));
            refinements.add(new StoryRefinement(story.id(), story.title(), refined));
        }
        return refinements;
    }
}
