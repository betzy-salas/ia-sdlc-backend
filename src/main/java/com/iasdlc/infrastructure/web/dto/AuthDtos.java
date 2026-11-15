package com.iasdlc.infrastructure.web.dto;

import com.iasdlc.domain.model.SquadRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 150, message = "admite máximo 150 caracteres")
            String fullName,
            @NotBlank(message = "es obligatorio") @Email(message = "debe ser un correo válido")
            @Size(max = 200, message = "admite máximo 200 caracteres")
            String email,
            @NotBlank(message = "es obligatoria")
            @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "es obligatorio") String email,
            @NotBlank(message = "es obligatoria") String password) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }

    public record UserResponse(UUID id, String fullName, String email, List<MembershipResponse> memberships) {
    }

    public record MembershipResponse(UUID squadId, String squadName, SquadRole role, boolean substitute) {
    }

    /** Elemento de GET /users. */
    public record UserSummaryResponse(UUID id, String fullName, String email) {
    }
}
