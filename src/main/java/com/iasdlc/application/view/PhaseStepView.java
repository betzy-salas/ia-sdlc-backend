package com.iasdlc.application.view;

import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.PhaseProgress;

/** Paso del recorrido de una iniciativa; {@code lastExecution} es null si la fase no se ha ejecutado. */
public record PhaseStepView(Phase phase, PhaseProgress progress, ExecutionView lastExecution) {
}
