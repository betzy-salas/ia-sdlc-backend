package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.ApprovalUseCase;
import com.iasdlc.application.port.out.ExecutionRepository;
import com.iasdlc.application.port.out.InitiativeRepository;
import com.iasdlc.application.port.out.PhaseRepository;
import com.iasdlc.application.port.out.ProjectRepository;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.Initiative;
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
    private final InitiativeRepository initiatives;
    private final ProjectRepository projects;
    private final SquadRepository squads;
    private final PhaseRepository phases;
    private final Clock clock;
    private final ViewAssembler views;

    public ApprovalService(ExecutionRepository executions, InitiativeRepository initiatives,
                           ProjectRepository projects, SquadRepository squads, PhaseRepository phases,
                           UserRepository users, Clock clock) {
        this.executions = executions;
        this.initiatives = initiatives;
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
            // El squad que aprueba es el del proyecto de la iniciativa.
            Initiative initiative = initiativeOf(execution);
            Project project = projectOf(initiative);
            if (GatePolicy.isApprover(squadOf(project), phaseOf(execution.phaseCode()), userId)) {
                pending.add(views.execution(execution, initiative, project));
            }
        }
        return pending;
    }

    @Override
    public ExecutionView decide(UUID executionId, UUID userId, Decision decision, String comment) {
        Execution execution = executions.findById(executionId)
                .orElseThrow(() -> DomainException.notFound("la ejecución"));
        Initiative initiative = initiativeOf(execution);
        Project project = projectOf(initiative);
        Squad squad = squadOf(project);
        Phase phase = phaseOf(execution.phaseCode());

        // Última ejecución anterior de la fase con resultado vigente (vienen de la más reciente a la más antigua).
        Optional<Execution> previousValid = executions.findByInitiativeId(initiative.id()).stream()
                .filter(e -> !e.id().equals(execution.id()))
                .filter(e -> e.phaseCode() == execution.phaseCode() && e.hasValidResult())
                .findFirst();

        GateDecisionService.decide(initiative, squad, phase, execution, userId, decision, comment,
                clock.instant(), previousValid);

        initiatives.save(initiative);
        return views.execution(executions.save(execution), initiative, project);
    }

    private Initiative initiativeOf(Execution execution) {
        return initiatives.findById(execution.initiativeId())
                .orElseThrow(() -> new IllegalStateException("La ejecución apunta a una iniciativa inexistente"));
    }

    private Project projectOf(Initiative initiative) {
        return projects.findById(initiative.projectId())
                .orElseThrow(() -> new IllegalStateException("La iniciativa apunta a un proyecto inexistente"));
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
