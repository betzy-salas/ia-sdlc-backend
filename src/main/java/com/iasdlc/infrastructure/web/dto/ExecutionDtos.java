package com.iasdlc.infrastructure.web.dto;

import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.CommonDtos.PersonRef;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.RefinedStoryResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ExecutionDtos {

    private ExecutionDtos() {
    }

    public record DecisionRequest(
            @NotNull(message = "es obligatoria") Decision decision,
            @Size(max = 4000, message = "admite máximo 4000 caracteres") String comment) {
    }

    public record ExecutionAgentRef(UUID id, String name, String version) {
    }

    public record ExecutionSummaryResponse(UUID id, NamedRef initiative, NamedRef project, PhaseCode phaseCode,
                                           ExecutionAgentRef agent, PersonRef launchedBy,
                                           ExecutionStatus status, Instant createdAt) {
    }

    /** executionSummary más el resultado del agente y la decisión del gate (null si no se ha decidido). */
    public record ExecutionResponse(UUID id, NamedRef initiative, NamedRef project, PhaseCode phaseCode,
                                    ExecutionAgentRef agent, PersonRef launchedBy, ExecutionStatus status,
                                    Instant createdAt,
                                    List<RefinedStoryItemResponse> refinedStories, DecisionResponse decision) {
    }

    public record RefinedStoryItemResponse(UUID storyId, String originalTitle, RefinedStoryResponse refined) {
    }

    public record DecisionResponse(Decision decision, PersonRef decidedBy, Instant decidedAt, String comment) {
    }
}
