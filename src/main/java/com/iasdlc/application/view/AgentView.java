package com.iasdlc.application.view;

import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.User;

/** Agente con su responsable; {@code owner} es null si el agente no tiene responsable (dato semilla). */
public record AgentView(Agent agent, User owner) {
}
