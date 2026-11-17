package com.iasdlc.infrastructure.web.dto;

import com.iasdlc.infrastructure.web.dto.CommonDtos.NamedRef;
import com.iasdlc.infrastructure.web.dto.InitiativeDtos.InitiativeSummaryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record CreateProjectRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 150, message = "admite máximo 150 caracteres")
            String name,
            @Size(max = 4000, message = "admite máximo 4000 caracteres") String description,
            @NotNull(message = "es obligatorio") UUID squadId) {
    }

    public record ProjectSummaryResponse(UUID id, String name, String description, NamedRef squad,
                                         int initiativeCount) {
    }

    public record ProjectResponse(UUID id, String name, String description, NamedRef squad,
                                  List<InitiativeSummaryResponse> initiatives) {
    }
}
