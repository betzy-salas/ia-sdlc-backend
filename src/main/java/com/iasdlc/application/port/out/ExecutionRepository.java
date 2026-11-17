package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.Execution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Las ejecuciones son históricas: el puerto no ofrece borrado. */
public interface ExecutionRepository {
    Execution save(Execution execution);
    Optional<Execution> findById(UUID id);
    /** Todas las ejecuciones, de la más reciente a la más antigua. */
    List<Execution> findAll();
    /** Ejecuciones de la iniciativa, de la más reciente a la más antigua. */
    List<Execution> findByInitiativeId(UUID initiativeId);
}
