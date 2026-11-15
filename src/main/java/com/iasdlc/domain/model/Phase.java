package com.iasdlc.domain.model;

/** Fase del proceso con el rol del squad que aprueba su gate. */
public record Phase(PhaseCode code, int order, String name, SquadRole approverRole) {
}
