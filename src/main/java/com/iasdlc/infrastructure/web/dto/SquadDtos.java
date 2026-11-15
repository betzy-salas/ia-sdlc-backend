package com.iasdlc.infrastructure.web.dto;

import com.iasdlc.domain.model.SquadRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class SquadDtos {

    private SquadDtos() {
    }

    public record CreateSquadRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 120, message = "admite máximo 120 caracteres")
            String name) {
    }

    /** {@code substitute} es opcional; si no llega se toma como titular (false). */
    public record AddMemberRequest(
            @NotNull(message = "es obligatorio") UUID userId,
            @NotNull(message = "es obligatorio") SquadRole role,
            Boolean substitute) {
    }

    public record SquadResponse(UUID id, String name, List<MemberResponse> members) {
    }

    public record MemberResponse(UUID id, UUID userId, String fullName, SquadRole role, boolean substitute) {
    }
}
