package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.PhaseQueryUseCase;
import com.iasdlc.application.port.out.AgentRepository;
import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.application.port.out.PhaseRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.view.DashboardPhaseView;
import com.iasdlc.application.view.PhaseView;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.Project;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@Transactional(readOnly = true)
public class PhaseQueryService implements PhaseQueryUseCase {

    private final PhaseRepository phases;
    private final AgentRepository agents;
    private final ProjectRepository projects;
    private final ExecutionRepository executions;

    public PhaseQueryService(PhaseRepository phases, AgentRepository agents, ProjectRepository projects,
                             ExecutionRepository executions) {
        this.phases = phases;
        this.agents = agents;
        this.projects = projects;
        this.executions = executions;
    }

    @Override
    public List<PhaseView> listPhases() {
        List<Agent> allAgents = agents.findAll();
        return phases.findAll().stream()
                .map(phase -> new PhaseView(phase, agentsOf(phase, allAgents)))
                .toList();
    }

    @Override
    public List<DashboardPhaseView> dashboard() {
        List<Agent> allAgents = agents.findAll();
        List<Project> allProjects = projects.findAll();
        List<Execution> allExecutions = executions.findAll();
        return phases.findAll().stream()
                .map(phase -> new DashboardPhaseView(
                        phase,
                        agentsOf(phase, allAgents),
                        allProjects.stream().filter(p -> p.currentPhase() == phase.code()).count(),
                        allExecutions.stream()
                                .filter(e -> e.phaseCode() == phase.code()
                                        && e.status() == ExecutionStatus.PENDING_APPROVAL)
                                .count()))
                .toList();
    }

    private static List<Agent> agentsOf(Phase phase, List<Agent> allAgents) {
        return allAgents.stream().filter(a -> a.phaseCode() == phase.code()).toList();
    }
}
