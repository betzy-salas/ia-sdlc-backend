package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.AgentUseCase;
import com.iasdlc.application.port.in.AgentUseCase.CreateAgentCommand;
import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.web.dto.AgentDtos.AddVersionRequest;
import com.iasdlc.infrastructure.web.dto.AgentDtos.AgentResponse;
import com.iasdlc.infrastructure.web.dto.AgentDtos.ChangeStatusRequest;
import com.iasdlc.infrastructure.web.dto.AgentDtos.CreateAgentRequest;
import com.iasdlc.infrastructure.web.mapper.AgentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentUseCase agents;

    public AgentController(AgentUseCase agents) {
        this.agents = agents;
    }

    @GetMapping
    public List<AgentResponse> list(@RequestParam(name = "phase", required = false) PhaseCode phase,
                                    @RequestParam(name = "status", required = false) AgentStatus status) {
        return agents.listAgents(phase, status).stream().map(AgentWebMapper::toListItem).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse create(@Valid @RequestBody CreateAgentRequest request,
                                @AuthenticationPrincipal UUID userId) {
        return AgentWebMapper.toDetail(agents.createAgent(new CreateAgentCommand(request.name(),
                request.phaseCode(), request.description(), request.repositoryUrl(), request.repositoryPath(),
                request.ownerUserId(), userId)));
    }

    @GetMapping("/{agentId}")
    public AgentResponse get(@PathVariable("agentId") UUID agentId) {
        return AgentWebMapper.toDetail(agents.getAgent(agentId));
    }

    @PostMapping("/{agentId}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse addVersion(@PathVariable("agentId") UUID agentId,
                                    @Valid @RequestBody AddVersionRequest request) {
        return AgentWebMapper.toDetail(agents.addVersion(agentId, request.version(), request.commitSha(),
                request.instructions(), request.changeNotes()));
    }

    @PatchMapping("/{agentId}/status")
    public AgentResponse changeStatus(@PathVariable("agentId") UUID agentId,
                                      @Valid @RequestBody ChangeStatusRequest request) {
        return AgentWebMapper.toDetail(agents.changeStatus(agentId, request.status()));
    }
}
