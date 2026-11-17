package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.ApprovalUseCase;
import com.iasdlc.application.port.in.ExecutionUseCase;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.DecisionRequest;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionResponse;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionSummaryResponse;
import com.iasdlc.infrastructure.web.mapper.ExecutionWebMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Consulta de ejecuciones y gate de aprobación. No hay rutas para editar ni borrar ejecuciones.
 */
@RestController
@RequestMapping("/api/v1")
public class ExecutionController {

    private final ExecutionUseCase executions;
    private final ApprovalUseCase approvals;

    public ExecutionController(ExecutionUseCase executions, ApprovalUseCase approvals) {
        this.executions = executions;
        this.approvals = approvals;
    }

    @GetMapping("/executions")
    public List<ExecutionSummaryResponse> list(
            @RequestParam(name = "initiativeId", required = false) UUID initiativeId,
            @RequestParam(name = "projectId", required = false) UUID projectId,
            @RequestParam(name = "status", required = false) ExecutionStatus status) {
        return executions.listExecutions(initiativeId, projectId, status).stream()
                .map(ExecutionWebMapper::toSummary)
                .toList();
    }

    @GetMapping("/executions/{executionId}")
    public ExecutionResponse get(@PathVariable("executionId") UUID executionId) {
        return ExecutionWebMapper.toResponse(executions.getExecution(executionId));
    }

    @GetMapping("/approvals/pending")
    public List<ExecutionResponse> pending(@AuthenticationPrincipal UUID userId) {
        return approvals.pendingApprovals(userId).stream().map(ExecutionWebMapper::toResponse).toList();
    }

    @PostMapping("/executions/{executionId}/decision")
    public ExecutionResponse decide(@PathVariable("executionId") UUID executionId,
                                    @Valid @RequestBody DecisionRequest request,
                                    @AuthenticationPrincipal UUID userId) {
        return ExecutionWebMapper.toResponse(
                approvals.decide(executionId, userId, request.decision(), request.comment()));
    }
}
