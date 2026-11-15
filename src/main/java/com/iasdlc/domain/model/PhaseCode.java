package com.iasdlc.domain.model;

import java.util.Optional;

/** Fases del SDLC, en el orden en que se recorren. */
public enum PhaseCode {
    REQUIREMENTS, ANALYSIS, DESIGN, CONSTRUCTION, TESTING;

    /** Fase siguiente, o vacío si es la última. */
    public Optional<PhaseCode> next() {
        PhaseCode[] all = values();
        return ordinal() + 1 < all.length ? Optional.of(all[ordinal() + 1]) : Optional.empty();
    }
}
