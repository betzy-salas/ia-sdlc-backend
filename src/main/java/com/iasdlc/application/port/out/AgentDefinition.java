package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.PhaseCode;

import java.util.UUID;

/**
 * Lo que el motor necesita saber del agente: su versión vigente, las instrucciones (agent.md)
 * y dónde vive en el repositorio.
 */
public record AgentDefinition(UUID agentId, String name, PhaseCode phaseCode, String version,
                              String instructions, String repositoryUrl, String repositoryPath) {
}
