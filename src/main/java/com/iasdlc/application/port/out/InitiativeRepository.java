package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.Initiative;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InitiativeRepository {
    Initiative save(Initiative initiative);
    Optional<Initiative> findById(UUID id);
    /** Todas las iniciativas, de la más reciente a la más antigua. */
    List<Initiative> findAll();
    /** Iniciativas del proyecto, de la más reciente a la más antigua. */
    List<Initiative> findByProjectId(UUID projectId);
    /** El nombre es único dentro del proyecto (sin distinguir mayúsculas). */
    boolean existsByProjectIdAndName(UUID projectId, String name);
}
