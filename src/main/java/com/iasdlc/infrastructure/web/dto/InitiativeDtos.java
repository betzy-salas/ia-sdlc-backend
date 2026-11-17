package com.iasdlc.infrastructure.web.dto;

import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.PhaseProgress;
import com.iasdlc.domain.model.SquadRole;
import com.iasdlc.domain.model.StoryOrigin;
import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.ExecutionDtos.ExecutionSummaryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class InitiativeDtos {

    private InitiativeDtos() {
    }

    public record CreateInitiativeRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 150, message = "admite máximo 150 caracteres")
            String name,
            @Size(max = 4000, message = "admite máximo 4000 caracteres") String description) {
    }

    public record CreateStoryRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 200, message = "admite máximo 200 caracteres")
            String title,
            @Size(max = 8000, message = "admite máximo 8000 caracteres") String description,
            @NotNull(message = "es obligatorio") StoryOrigin origin) {
    }

    public record InitiativeSummaryResponse(UUID id, String name, NamedRef project, NamedRef squad,
                                            PhaseCode currentPhase, boolean completed, int storyCount,
                                            Instant createdAt) {
    }

    public record InitiativeResponse(UUID id, String name, String description, NamedRef project, NamedRef squad,
                                     PhaseCode currentPhase, boolean completed, List<StoryResponse> stories,
                                     List<PhaseStepResponse> journey, Instant createdAt) {
    }

    public record StoryResponse(UUID id, String title, String description, StoryOrigin origin,
                                RefinedStoryResponse refined) {
    }

    public record RefinedStoryResponse(String title, String asA, String iWant, String soThat,
                                       List<String> acceptanceCriteria, List<String> ambiguities) {
    }

    public record PhaseStepResponse(PhaseCode phaseCode, int order, String name, PhaseProgress progress,
                                    SquadRole approverRole, ExecutionSummaryResponse lastExecution) {
    }
}
