package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.ApprovalUseCase;
import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.application.port.out.PhaseRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.service.GateDecisionService;
import com.iasdlc.domain.service.GatePolicy;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@UseCase
@Transactional
public class ApprovalService implements ApprovalUseCase {

    private final ExecutionRepository executions;
    private final ProjectRepository projects;
    private final SquadRepository squads;
    private final PhaseRepository phases;
    private final Clock clock;
    private final ViewAssembler views;

    public ApprovalService(ExecutionRepository executions, ProjectRepository projects, SquadRepository squads,
                           PhaseRepository phases, UserRepository users, Clock clock) {
        this.executions = executions;
        this.projects = projects;
        this.squads = squads;
        this.phases = phases;
        this.clock = clock;
        this.views = new ViewAssembler(users, squads);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionView> pendingApprovals(UUID userId) {
        List<ExecutionView> pending = new ArrayList<>();
        for (Execution execution : executions.findAll()) {
            if (execution.status() != ExecutionStatus.PENDING_APPROVAL) {
                continue;
            }
            Project project = projectOf(execution);
            if (GatePolicy.isApprover(squadOf(project), phaseOf(execution.phaseCode()), userId)) {
                pending.add(views.execution(execution, project.name()));
            }
        }
        return pending;
    }

    @Override
    public ExecutionView decide(UUID executionId, UUID userId, Decision decision, String comment) {
        Execution execution = executions.findById(executionId)
                .orElseThrow(() -> DomainException.notFound("la ejecución"));
        Project project = projectOf(execution);
        Squad squad = squadOf(project);
        Phase phase = phaseOf(execution.phaseCode());

        // Última ejecución anterior de la fase con resultado vigente (vienen de la más reciente a la más antigua).
        Optional<Execution> previousValid = executions.findByProjectId(project.id()).stream()
                .filter(e -> !e.id().equals(execution.id()))
                .filter(e -> e.phaseCode() == execution.phaseCode() && e.hasValidResult())
                .findFirst();

        GateDecisionService.decide(project, squad, phase, execution, userId, decision, comment,
                clock.instant(), previousValid);

        projects.save(project);
        return views.execution(executions.save(execution), project.name());
    }

    private Project projectOf(Execution execution) {
        return projects.findById(execution.projectId())
                .orElseThrow(() -> new IllegalStateException("La ejecución apunta a un proyecto inexistente"));
    }

    private Squad squadOf(Project project) {
        return squads.findById(project.squadId())
                .orElseThrow(() -> new IllegalStateException("El proyecto apunta a un squad inexistente"));
    }

    private Phase phaseOf(PhaseCode code) {
        return phases.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Falta la fase " + code + " en el catálogo"));
    }
}
