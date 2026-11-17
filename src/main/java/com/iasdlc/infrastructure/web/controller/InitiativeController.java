package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.ExecutionUseCase;
import com.iasdlc.application.port.in.InitiativeUseCase;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionResponse;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.CreateStoryRequest;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.InitiativeResponse;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.InitiativeSummaryResponse;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.StoryResponse;
import com.iasdlc.infrastructure.web.mapper.ExecutionWebMapper;
import com.iasdlc.infrastructure.web.mapper.InitiativeWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Iniciativas: la unidad que recorre las fases. De aquí cuelgan las historias y la ejecución
 * de la fase. La iniciativa se crea bajo su proyecto (POST /projects/{projectId}/initiatives).
 */
@RestController
@RequestMapping("/api/v1/initiatives")
public class InitiativeController {

    private final InitiativeUseCase initiatives;
    private final ExecutionUseCase executions;

    public InitiativeController(InitiativeUseCase initiatives, ExecutionUseCase executions) {
        this.initiatives = initiatives;
        this.executions = executions;
    }

    @GetMapping
    public List<InitiativeSummaryResponse> list(
            @RequestParam(name = "projectId", required = false) UUID projectId,
            @RequestParam(name = "phase", required = false) PhaseCode phase) {
        return initiatives.listInitiatives(projectId, phase).stream().map(InitiativeWebMapper::toSummary).toList();
    }

    @GetMapping("/{initiativeId}")
    public InitiativeResponse get(@PathVariable("initiativeId") UUID initiativeId) {
        return InitiativeWebMapper.toResponse(initiatives.getInitiative(initiativeId));
    }

    @PostMapping("/{initiativeId}/stories")
    @ResponseStatus(HttpStatus.CREATED)
    public StoryResponse addStory(@PathVariable("initiativeId") UUID initiativeId,
                                  @Valid @RequestBody CreateStoryRequest request) {
        return InitiativeWebMapper.toResponse(
                initiatives.addStory(initiativeId, request.title(), request.description(), request.origin()));
    }

    @DeleteMapping("/{initiativeId}/stories/{storyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStory(@PathVariable("initiativeId") UUID initiativeId,
                            @PathVariable("storyId") UUID storyId) {
        initiatives.removeStory(initiativeId, storyId);
    }

    /**
     * Ejecuta el agente de la fase sobre las historias de la iniciativa. En esta entrega solo se
     * admite REQUIREMENTS; la ejecución es síncrona y no lleva cuerpo.
     */
    @PostMapping("/{initiativeId}/phases/{phaseCode}/executions")
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionResponse runPhase(@PathVariable("initiativeId") UUID initiativeId,
                                      @PathVariable("phaseCode") PhaseCode phaseCode,
                                      @AuthenticationPrincipal UUID userId) {
        return ExecutionWebMapper.toResponse(executions.runPhase(initiativeId, phaseCode, userId));
    }
}
