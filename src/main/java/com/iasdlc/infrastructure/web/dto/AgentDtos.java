package com.iasdlc.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.infrastructure.web.dto.CommonDtos.PersonRef;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AgentDtos {

    private AgentDtos() {
    }

    /** {@code ownerUserId} es opcional; si no llega, el responsable es quien crea el agente. */
    public record CreateAgentRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 120, message = "admite máximo 120 caracteres")
            String name,
            @NotNull(message = "es obligatorio") PhaseCode phaseCode,
            @Size(max = 4000, message = "admite máximo 4000 caracteres") String description,
            @Size(max = 500, message = "admite máximo 500 caracteres") String repositoryUrl,
            @Size(max = 500, message = "admite máximo 500 caracteres") String repositoryPath,
            UUID ownerUserId) {
    }

    public record AddVersionRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 30, message = "admite máximo 30 caracteres")
            String version,
            @Size(max = 64, message = "admite máximo 64 caracteres") String commitSha,
            @NotBlank(message = "es obligatorio") String instructions,
            String changeNotes) {
    }

    public record ChangeStatusRequest(@NotNull(message = "es obligatorio") AgentStatus status) {
    }

    /** {@code owner} es null en el agente semilla, que no tiene responsable. */
    public record AgentResponse(UUID id, String name, PhaseCode phaseCode, AgentStatus status,
                                String currentVersion, String description, String repositoryUrl,
                                String repositoryPath, PersonRef owner, List<AgentVersionResponse> versions) {
    }

    /** {@code instructions} solo se incluye en las respuestas de un agente individual, no en el listado. */
    public record AgentVersionResponse(String version, String commitSha, String changeNotes, Instant createdAt,
                                       @JsonInclude(JsonInclude.Include.NON_NULL) String instructions) {
    }
}
