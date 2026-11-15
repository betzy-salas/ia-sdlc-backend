package com.iasdlc.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Decisión del gate sobre una ejecución. Una vez registrada no cambia. */
public record GateDecision(Decision decision, UUID decidedBy, Instant decidedAt, String comment) {
}
