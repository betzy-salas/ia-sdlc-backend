package com.iasdlc.application.view;

import com.iasdlc.domain.model.Execution;

import java.util.UUID;

/** Ejecución con los nombres que la capa web necesita mostrar (iniciativa, proyecto y personas). */
public record ExecutionView(Execution execution, String initiativeName, UUID projectId, String projectName,
                            String launchedByName, String decidedByName) {
}
