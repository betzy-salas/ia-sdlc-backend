package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.Squad;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SquadRepository {
    Squad save(Squad squad);
    Optional<Squad> findById(UUID id);
    /** Todos los squads ordenados por nombre. */
    List<Squad> findAll();
    boolean existsByName(String name);
    /** Squads en los que el usuario es integrante (con cualquier rol). */
    List<Squad> findByMemberUserId(UUID userId);
}
