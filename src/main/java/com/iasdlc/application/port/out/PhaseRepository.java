package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.PhaseCode;

import java.util.List;
import java.util.Optional;

public interface PhaseRepository {
    /** Fases en orden. */
    List<Phase> findAll();
    Optional<Phase> findByCode(PhaseCode code);
}
