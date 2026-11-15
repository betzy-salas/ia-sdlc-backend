package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.AgentUseCase;
import com.iasdlc.application.port.out.AgentRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.AgentView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@UseCase
@Transactional
public class AgentService implements AgentUseCase {

    private final AgentRepository agents;
    private final UserRepository users;
    private final Clock clock;

    public AgentService(AgentRepository agents, UserRepository users, Clock clock) {
        this.agents = agents;
        this.users = users;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentView> listAgents(PhaseCode phase, AgentStatus status) {
        return agents.findAll().stream()
                .filter(a -> phase == null || a.phaseCode() == phase)
                .filter(a -> status == null || a.status() == status)
                .map(this::view)
                .toList();
    }

    @Override
    public AgentView createAgent(CreateAgentCommand command) {
        if (agents.existsByName(command.name().trim())) {
            throw new DomainException(ErrorCode.DUPLICATE_NAME, "Ya existe un agente con ese nombre");
        }
        UUID ownerId = command.ownerUserId() != null ? command.ownerUserId() : command.requestedBy();
        User owner = users.findById(ownerId)
                .orElseThrow(() -> DomainException.notFound("el usuario responsable del agente"));
        Agent agent = Agent.create(command.name(), command.phaseCode(), command.description(),
                command.repositoryUrl(), command.repositoryPath(), owner.id(), clock.instant());
        return new AgentView(agents.save(agent), owner);
    }

    @Override
    @Transactional(readOnly = true)
    public AgentView getAgent(UUID agentId) {
        return view(load(agentId));
    }

    @Override
    public AgentView addVersion(UUID agentId, String version, String commitSha, String instructions,
                                String changeNotes) {
        Agent agent = load(agentId);
        agent.addVersion(version, commitSha, instructions, changeNotes, clock.instant());
        return view(agents.save(agent));
    }

    @Override
    public AgentView changeStatus(UUID agentId, AgentStatus status) {
        Agent agent = load(agentId);
        agent.changeStatus(status);
        return view(agents.save(agent));
    }

    private Agent load(UUID agentId) {
        return agents.findById(agentId).orElseThrow(() -> DomainException.notFound("el agente"));
    }

    private AgentView view(Agent agent) {
        User owner = agent.ownerUserId() == null ? null : users.findById(agent.ownerUserId()).orElse(null);
        return new AgentView(agent, owner);
    }
}
