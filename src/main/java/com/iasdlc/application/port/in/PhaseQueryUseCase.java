package com.iasdlc.application.port.in;

import com.iasdlc.application.view.DashboardPhaseView;
import com.iasdlc.application.view.PhaseView;

import java.util.List;

public interface PhaseQueryUseCase {
    /** Fases en orden, cada una con sus agentes. */
    List<PhaseView> listPhases();
    /** Tablero de inicio: por fase, agentes, proyectos en la fase y aprobaciones pendientes. */
    List<DashboardPhaseView> dashboard();
}
