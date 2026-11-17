package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.Agent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRepository {
    Agent save(Agent agent);
    Optional<Agent> findById(UUID id);
    /** Todos los agentes ordenados por nombre. */
    List<Agent> findAll();
    boolean existsByName(String name);
}
