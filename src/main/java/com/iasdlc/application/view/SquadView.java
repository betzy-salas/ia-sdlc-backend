package com.iasdlc.application.view;

import com.iasdlc.domain.model.Squad;

import java.util.Map;
import java.util.UUID;

/** Squad con el nombre de cada usuario integrante (userId -> nombre completo). */
public record SquadView(Squad squad, Map<UUID, String> userNames) {
}
