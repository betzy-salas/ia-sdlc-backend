package com.iasdlc.domain.service;

import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.Squad;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aplica la decisión de un gate sobre la ejecución y la iniciativa.
 * El squad es el del proyecto al que pertenece la iniciativa.
 */
public final class GateDecisionService {

    private GateDecisionService() {
    }

    /**
     * @param previousValid última ejecución anterior de la misma fase cuyo resultado sigue vigente
     *                      (no rechazada ni fallida); se usa para restaurar las historias si se rechaza.
     */
    public static void decide(Initiative initiative, Squad squad, Phase phase, Execution execution,
                              UUID userId, Decision decision, String comment, Instant now,
                              Optional<Execution> previousValid) {
        GatePolicy.ensureApprover(squad, phase, userId);
        execution.decide(decision, userId, comment, now);
        switch (decision) {
            case APPROVE -> initiative.approvePhase(execution.phaseCode());
            // El resultado sigue visible en las historias mientras se rehace.
            case REQUEST_REWORK -> initiative.reopenPhase(execution.phaseCode());
            case REJECT -> {
                initiative.reopenPhase(execution.phaseCode());
                // story.refined refleja la última ejecución no rechazada (o nada si no hay).
                initiative.applyRefinements(previousValid.map(Execution::refinedStories).orElse(List.of()));
            }
        }
    }
}
