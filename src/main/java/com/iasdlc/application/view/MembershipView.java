package com.iasdlc.application.view;

import com.iasdlc.domain.model.SquadRole;

import java.util.UUID;

public record MembershipView(UUID squadId, String squadName, SquadRole role, boolean substitute) {
}
