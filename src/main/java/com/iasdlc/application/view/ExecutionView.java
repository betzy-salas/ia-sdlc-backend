package com.iasdlc.application.view;

import com.iasdlc.domain.model.Execution;

/** Ejecución con los nombres que la capa web necesita mostrar. */
public record ExecutionView(Execution execution, String projectName, String launchedByName,
                            String decidedByName) {
}
